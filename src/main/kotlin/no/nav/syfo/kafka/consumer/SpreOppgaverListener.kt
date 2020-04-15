package no.nav.syfo.kafka.consumer

import no.nav.syfo.config.unleash.ToggleImpl
import no.nav.syfo.domain.DokumentTypeDTO.Søknad
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.log
import no.nav.syfo.service.SpreOppgaverService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException

@Component
class SpreOppgaverListener(private val spreOppgaverService: SpreOppgaverService,
                        private val toggle: ToggleImpl) {
    private val log = log()

    @KafkaListener(topics = ["aapen-helse-spre-oppgaver"], id = "syfogsakListener", idIsGroup = false, containerFactory = "spreOppgaverContainerFactory")
    fun listen(cr: ConsumerRecord<String, OppgaveDTO>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

            val oppgave = cr.value()

            if(oppgave.dokumentType == Søknad) {
                log.info("Gjelder ${oppgave.oppdateringstype.name} for søknad ${oppgave.dokumentId}")
                when(oppgave.oppdateringstype) {
                    OppdateringstypeDTO.Utsett -> spreOppgaverService.utsettOppgave(oppgave.dokumentId.toString(), oppgave.timeout!!)
                    OppdateringstypeDTO.Opprett -> spreOppgaverService.opprettOppgave(id = oppgave.dokumentId.toString())
                    OppdateringstypeDTO.Ferdigbehandlet -> spreOppgaverService.viBehandlerIkkeOppgaven(oppgave.dokumentId.toString())
                }
            }

            acknowledgment.acknowledge()
        } catch(e: HttpClientErrorException) {
            if (toggle.isQ() && e.rawStatusCode == 404) {
                log.warn("Søknaden er slettet fra Q, hopper over og fortsetter")
            }
            else {
                log.error("Rest kall feiler", e)
                throw RuntimeException("Rest kall feiler", e)
            }
        } catch (e: Exception) {
            log.error("Uventet feil ved lesing fra ${cr.topic()}", e)
            throw RuntimeException("Uventet feil ved lesing fra ${cr.topic()}")
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }
}

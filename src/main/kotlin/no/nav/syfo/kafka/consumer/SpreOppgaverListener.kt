package no.nav.syfo.kafka.consumer

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.log
import no.nav.syfo.service.OppgaveKilde
import no.nav.syfo.service.SpreOppgaverService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class SpreOppgaverListener(
    private val spreOppgaverService: SpreOppgaverService,
    private val registry: MeterRegistry,
) {
    private val log = log()

    @KafkaListener(topics = ["aapen-helse-spre-oppgaver"], id = "syfogsakListener", idIsGroup = false, containerFactory = "spreOppgaverContainerFactory")
    fun listen(cr: ConsumerRecord<String, OppgaveDTO>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))
            val oppgave = cr.value()
            if (oppgave.dokumentType == DokumentTypeDTO.Søknad) {
                log.info("Mottok spre oppgave: $oppgave")
                spreOppgaverService.prosesserOppgave(cr.value(), OppgaveKilde.Saksbehandling)
                tellOppgave(oppgave)
            }
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Uventet feil ved lesing fra ${cr.topic()}", e)
            throw RuntimeException("Uventet feil ved lesing fra ${cr.topic()}")
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }

    private fun tellOppgave(oppgave: OppgaveDTO) {
        registry.counter(
            "syfogsak.spre.oppgave",
            Tags.of(
                "type", "info",
                "oppdateringstype", oppgave.oppdateringstype.name
            )
        ).increment()
    }
}

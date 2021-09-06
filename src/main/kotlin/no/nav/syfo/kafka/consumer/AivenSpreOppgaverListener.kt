package no.nav.syfo.kafka.consumer

import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.logger
import no.nav.syfo.objectMapper
import no.nav.syfo.service.OppgaveKilde
import no.nav.syfo.service.SpreOppgaverService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

const val SPREOPPGAVER_TOPIC = "tbd." + "spre-oppgaver"

@Component
class AivenSpreOppgaverListener(
    private val spreOppgaverService: SpreOppgaverService,
    private val registry: MeterRegistry,
) {

    private val log = logger()

    @KafkaListener(
        topics = [SPREOPPGAVER_TOPIC],
        id = "spreOppgave",
        idIsGroup = false,
        containerFactory = "aivenKafkaListenerContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {

        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))
            val oppgaveDTO = cr.value().tilSpreOppgaveDTO()

            if (oppgaveDTO.dokumentType == DokumentTypeDTO.Søknad) {
                log.info("Mottok spre oppgave på aiven: $oppgaveDTO")
                spreOppgaverService.prosesserOppgave(oppgaveDTO, OppgaveKilde.Saksbehandling)
                tellOppgave(oppgaveDTO)
            } else {
                log.info("Ignorerer oppgave med dokumentId ${oppgaveDTO.dokumentId}")
            }
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Uventet feil ved prosessering av oppgave", e)
            throw RuntimeException("Uventet feil ved rosessering av oppgave")
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

    fun String.tilSpreOppgaveDTO(): OppgaveDTO = objectMapper.readValue(this)
}

package no.nav.helse.flex.kafka.consumer

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.util.TimedCache
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class RekkefolgeListener {

    private val log = logger()

    @KafkaListener(
        topics = [SPREOPPGAVER_TOPIC],
        id = "rekkefolge",
        idIsGroup = true,
        containerFactory = "aivenKafkaListenerContainerFactory",
        properties = ["auto.offset.reset=latest"]
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val oppgaveDTO = cr.value().tilSpreOppgaveDTO()

        if (oppgaveDTO.dokumentType == DokumentTypeDTO.Søknad) {
            val partisjon = cr.partition()
            val forrige = TimedCache.put(oppgaveDTO.dokumentId, partisjon)
            if (forrige != null && forrige != partisjon) {
                log.info("Spre oppgave for søknad ${oppgaveDTO.dokumentId} er fordelt på partisjon $forrige og $partisjon")
            }
        }

        acknowledgment.acknowledge()
    }

    fun String.tilSpreOppgaveDTO(): OppgaveDTO = objectMapper.readValue(this)
}

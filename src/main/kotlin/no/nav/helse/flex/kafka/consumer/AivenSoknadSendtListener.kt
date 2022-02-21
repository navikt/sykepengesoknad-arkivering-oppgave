package no.nav.helse.flex.kafka.consumer

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.kafka.mapper.toSykepengesoknad
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.service.IdentService
import no.nav.helse.flex.service.SpreOppgaverService
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

const val SYKEPENGESOKNAD_TOPIC = "flex." + "sykepengesoknad"

@Component
class AivenSoknadSendtListener(
    private val spreOppgaverService: SpreOppgaverService,
    private val identService: IdentService,
) {

    private val log = logger()

    @KafkaListener(
        topics = [SYKEPENGESOKNAD_TOPIC],
        id = "soknadSendt",
        idIsGroup = false,
        containerFactory = "aivenKafkaListenerContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

            val dto = cr.value().tilSykepengesoknadDTO()

            val aktorId = identService.hentAktorIdForFnr(dto.fnr)

            val sykepengesoknad = dto.toSykepengesoknad(aktorId)
            spreOppgaverService.soknadSendt(sykepengesoknad)

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av søknad", e)
            throw RuntimeException("Uventet feil ved behandling av søknad")
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
}

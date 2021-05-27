package no.nav.syfo.kafka.consumer

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.logger
import no.nav.syfo.objectMapper
import no.nav.syfo.service.IdentService
import no.nav.syfo.service.SpreOppgaverService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.context.event.EventListener
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.event.ConsumerStoppedEvent
import org.springframework.kafka.listener.KafkaMessageListenerContainer
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

    @EventListener
    fun eventHandler(event: ConsumerStoppedEvent) {
        if (event.reason == ConsumerStoppedEvent.Reason.NORMAL) {
            return
        }
        log.error("Consumer stoppet grunnet ${event.reason}")
        if (event.source is KafkaMessageListenerContainer<*, *> &&
            event.reason == ConsumerStoppedEvent.Reason.AUTH
        ) {
            val container = event.source as KafkaMessageListenerContainer<*, *>
            log.info("Trying to restart consumer, creds may be rotated")
            container.start()
        }
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
}

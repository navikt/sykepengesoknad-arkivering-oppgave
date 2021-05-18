package no.nav.syfo.kafka.consumer

import no.nav.syfo.OVERGANG
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.felles.DeprecatedSykepengesoknadDTO
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.logger
import no.nav.syfo.service.SpreOppgaverService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import javax.inject.Inject

@Component
class SoknadSendtListener @Inject
constructor(private val spreOppgaverService: SpreOppgaverService) {
    private val log = logger()

    @KafkaListener(
        topics = ["syfo-soknad-v2", "syfo-soknad-v3"],
        id = "soknadSendt",
        idIsGroup = false,
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, DeprecatedSykepengesoknadDTO>, acknowledgment: Acknowledgment) {
        val nå = LocalDateTime.now()
        if (nå >= OVERGANG) {
            if (nå <= OVERGANG.plusMinutes(10)) {
                log.info("on-prem behandler ikke ${cr.key()}")
            }
            acknowledgment.acknowledge()
            return
        }

        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

            val value = cr.value()

            spreOppgaverService.soknadSendt(value.toSykepengesoknad())

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av søknad", e)
            throw RuntimeException("Uventet feil ved behandling av søknad")
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }
}

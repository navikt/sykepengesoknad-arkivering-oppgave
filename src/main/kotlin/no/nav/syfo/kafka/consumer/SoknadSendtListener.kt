package no.nav.syfo.kafka.consumer

import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.service.SpreOppgaverService
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class SoknadSendtListener @Inject
constructor(private val spreOppgaverService: SpreOppgaverService) {
    private val log = log()

    @KafkaListener(
            topics = ["syfo-soknad-v2", "syfo-soknad-v3"],
            id = "soknadSendt",
            idIsGroup = false,
            containerFactory = "kafkaListenerContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, SykepengesoknadDTO>, acknowledgment: Acknowledgment) {
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

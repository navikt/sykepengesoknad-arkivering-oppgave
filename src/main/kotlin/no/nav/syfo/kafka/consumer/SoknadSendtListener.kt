package no.nav.syfo.kafka.consumer

import no.nav.syfo.config.CALL_ID
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import no.nav.syfo.service.SaksbehandlingsService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID
import javax.inject.Inject

@Component
class SoknadSendtListener @Inject
constructor(private val saksbehandlingsService: SaksbehandlingsService) {

    @KafkaListener(topics = ["syfo-soknad-v2"], id = "soknadSendt", idIsGroup = false, containerFactory = "soknadContainerFactory")
    fun listen(cr: ConsumerRecord<String, Soknad>, acknowledgment: Acknowledgment) {
        log().debug("Melding mottatt på topic: {}, partisjon: {} med offset: {}", cr.topic(), cr.partition(), cr.offset())

        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID).orElseGet { randomUUID().toString() })

            val soknad = cr.value()

            var sykepengesoknad: Sykepengesoknad? = null
            if (soknad is SoknadDTO) {
                sykepengesoknad = soknad.toSykepengesoknad()
            } else if (soknad is SykepengesoknadDTO) {
                sykepengesoknad = soknad.toSykepengesoknad()
            }
            sykepengesoknad?.let { saksbehandlingsService.behandleSoknad(it) }

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log().error("Uventet feil ved behandling av søknad", e)
            throw RuntimeException("Uventet feil ved behandling av søknad")
        } finally {
            MDC.remove(CALL_ID)
        }
    }
}

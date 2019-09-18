package no.nav.syfo.kafka

import no.nav.syfo.config.CALL_ID
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SykepengesoknadArbeidsledigDTO
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
class SykepengesoknadarbeidsledigSendtListener @Inject
constructor(private val saksbehandlingsService: SaksbehandlingsService) {
    private val log = log()

    @KafkaListener(topics = ["syfo-soknad-arbeidsledig-v1"], id = "soknadArbeidsledigSendt", idIsGroup = false)
    fun listen(cr: ConsumerRecord<String, SykepengesoknadArbeidsledigDTO>, acknowledgment: Acknowledgment) {
        log.debug("Melding om søknad for arbeidsledig er mottatt på topic: {}, partisjon: {} med offset: {}", cr.topic(), cr.partition(), cr.offset())

        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID).orElseGet { randomUUID().toString() })

            val soknad = cr.value()
            val sykepengesoknad: Sykepengesoknad = soknad.toSykepengesoknad()

            saksbehandlingsService.behandleSoknad(sykepengesoknad)

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av arbeidsledig søknad", e)
            throw RuntimeException("Uventet feil ved behandling av arbeidsledig søknad")
        } finally {
            MDC.remove(CALL_ID)
        }
    }
}

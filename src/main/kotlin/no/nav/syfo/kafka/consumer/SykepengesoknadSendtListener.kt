package no.nav.syfo.kafka.consumer

import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import no.nav.syfo.service.SaksbehandlingsService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class SykepengesoknadSendtListener @Inject
constructor(private val saksbehandlingsService: SaksbehandlingsService) {
    private val log = log()

    @KafkaListener(topics = ["syfo-soknad-v3"], id = "sykepengesoknadSendt", idIsGroup = false, containerFactory = "sykepengesoknadContainerFactory")
    fun listen(cr: ConsumerRecord<String, SykepengesoknadDTO>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

            val soknad = cr.value()
            val sykepengesoknad: Sykepengesoknad = soknad.toSykepengesoknad()

            saksbehandlingsService.behandleSoknad(sykepengesoknad)

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av søknad", e)
            throw RuntimeException("Uventet feil ved behandling av søknad")
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }
}

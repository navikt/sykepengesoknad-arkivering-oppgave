package no.nav.syfo.kafka.consumer

import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
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

    @KafkaListener(
        topics = ["syfo-soknad-v2"],
        id = "soknadSendt",
        idIsGroup = false,
        containerFactory = "soknadContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, Soknad>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

            when (val soknad = cr.value()) {
                is SoknadDTO -> saksbehandlingsService.behandleSoknad(soknad.toSykepengesoknad())
                is SykepengesoknadDTO -> saksbehandlingsService.behandleSoknad(soknad.toSykepengesoknad())
            }

            acknowledgment.acknowledge()
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }
}

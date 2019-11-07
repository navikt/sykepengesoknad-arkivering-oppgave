package no.nav.syfo.kafka.consumer

import no.nav.syfo.config.CALL_ID
import no.nav.syfo.kafka.getLastHeaderByKeyAsString
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
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID) ?: randomUUID().toString() )

            val soknad = cr.value()
            when (soknad) {
                is SoknadDTO -> saksbehandlingsService.behandleSoknad(soknad.toSykepengesoknad())
                is SykepengesoknadDTO -> saksbehandlingsService.behandleSoknad(soknad.toSykepengesoknad())
            }

            acknowledgment.acknowledge()
        } finally {
            MDC.remove(CALL_ID)
        }
    }
}

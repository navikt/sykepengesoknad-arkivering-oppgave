package no.nav.syfo.kafka.consumer

import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.service.SaksbehandlingsService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class SoknadSendtListener @Inject
constructor(private val saksbehandlingsService: SaksbehandlingsService) {

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
            saksbehandlingsService.behandleSoknad(value.toSykepengesoknad())

            acknowledgment.acknowledge()
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }
}

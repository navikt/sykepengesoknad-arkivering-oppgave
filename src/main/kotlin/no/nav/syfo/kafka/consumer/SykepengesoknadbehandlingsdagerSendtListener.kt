package no.nav.syfo.kafka.consumer

import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.kafka.sykepengesoknadbehandlingsdager.dto.SykepengesoknadBehandlingsdagerDTO
import no.nav.syfo.log
import no.nav.syfo.service.SaksbehandlingsService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class SykepengesoknadbehandlingsdagerSendtListener @Inject
constructor(private val saksbehandlingsService: SaksbehandlingsService) {
    private val log = log()

    @KafkaListener(topics = ["syfo-soknad-behandlingsdager-v1"], id = "soknadBehandlingsdagerSendt", idIsGroup = false, containerFactory = "behandlingsdagerContainerFactory")
    fun listen(cr: ConsumerRecord<String, SykepengesoknadBehandlingsdagerDTO>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

            val soknad = cr.value()
            val sykepengesoknad: Sykepengesoknad = soknad.toSykepengesoknad()

            saksbehandlingsService.behandleSoknad(sykepengesoknad)

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av BEHANDLINGSDAGER søknad", e)
            throw RuntimeException("Uventet feil ved behandling av BEHANDLINGSDAGER søknad")
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }
}

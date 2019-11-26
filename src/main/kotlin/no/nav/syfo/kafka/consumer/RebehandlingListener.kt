package no.nav.syfo.kafka.consumer

import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.kafka.producer.RebehandlingProducer
import no.nav.syfo.log
import no.nav.syfo.service.BehandleFeiledeSoknaderService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@Component
class RebehandlingListener @Inject
constructor(private val behandleFeiledeSoknaderService: BehandleFeiledeSoknaderService,
            private val innsendingDAO: InnsendingDAO,
            private val rebehandlingProducer: RebehandlingProducer) {
    val log = log()

    @KafkaListener(topics = ["syfogsak-rebehandle-soknad-v1"], id = "syfogsak-rebehandling", idIsGroup = false, containerFactory = "rebehandlingContainerFactory")
    fun listen(cr: ConsumerRecord<String, Sykepengesoknad>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))
            cr.headers().lastHeader(BEHANDLINGSTIDSPUNKT)
                ?.value()
                ?.let { String(it, StandardCharsets.UTF_8) }
                ?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
                ?.takeIf { now().isBefore(it) }
                ?.apply {
                    log.info("Plukket opp søknad ${cr.key()} for rebehandling med senere behandlingstidspunkt, venter 1 minutt og legger tilbake på kø...")
                    Thread.sleep(60000)
                    rebehandlingProducer.leggPaRebehandlingTopic(cr.value() as Sykepengesoknad, this)
                    acknowledgment.acknowledge()
                    return
                }

            val sykepengesoknad = cr.value() as Sykepengesoknad
            val innsending = innsendingDAO.finnInnsendingForSykepengesoknad(sykepengesoknad.id)!!
            behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad)
            acknowledgment.acknowledge()
            log.info("Søknad med id: ${sykepengesoknad.id} fullførte rebehandling")
        } catch (e: Exception) {
            val sykepengesoknad = cr.value() as Sykepengesoknad
            log.error("Uventet feil ved rebehandling av søknad ${sykepengesoknad.id}, legger søknaden tilbake på rebehandling-topic", e)
            rebehandlingProducer.leggPaRebehandlingTopic(sykepengesoknad, now().plusMinutes(10))
            acknowledgment.acknowledge()
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }
}

package no.nav.syfo.kafka.consumer

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.producer.RebehandleSykepengesoknadProducer
import no.nav.syfo.logger
import no.nav.syfo.objectMapper
import no.nav.syfo.service.BehandleFeiledeSoknaderService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject

const val RETRY_TOPIC = "flex." + "syfogsak-retry"

@Component
class RebehandleSykepengesoknadListener @Inject
constructor(
    private val behandleFeiledeSoknaderService: BehandleFeiledeSoknaderService,
    private val innsendingDAO: InnsendingDAO,
    private val rebebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer
) {
    val log = logger()

    @KafkaListener(
        topics = [RETRY_TOPIC],
        containerFactory = "aivenKafkaListenerContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val sykepengesoknad = cr.value().tilSykepengesoknad()
        val behandlingstidspunkt = cr.headers().lastHeader(BEHANDLINGSTIDSPUNKT)
            ?.value()
            ?.let { String(it, StandardCharsets.UTF_8) }
            ?.let { Instant.ofEpochMilli(it.toLong()) }
            ?: Instant.now()

        try {
            val sovetid = behandlingstidspunkt.toEpochMilli() - Instant.now().toEpochMilli()
            if (sovetid > 0) {
                log.info(
                    "Mottok rebehandling av søknad ${sykepengesoknad.id} med behandlingstidspunkt ${
                    behandlingstidspunkt.atOffset(
                        ZoneOffset.UTC
                    )
                    } sover i $sovetid millisekunder"
                )
                acknowledgment.nack(sovetid)
            } else {
                val innsending = innsendingDAO.finnInnsendingForSykepengesoknad(sykepengesoknad.id)
                behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad)
                acknowledgment.acknowledge()
            }
        } catch (e: Exception) {
            log.error("Uventet feil ved rebehandling av søknad ${sykepengesoknad.id}, legger søknaden tilbake på rebehandling-topic", e)
            rebebehandleSykepengesoknadProducer.send(sykepengesoknad)
            acknowledgment.acknowledge()
        }
    }

    fun String.tilSykepengesoknad(): Sykepengesoknad = objectMapper.readValue(this)
}
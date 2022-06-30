package no.nav.helse.flex.kafka.consumer

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.BEHANDLINGSTIDSPUNKT
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.kafka.producer.RebehandleSykepengesoknadProducer
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.service.BehandleFeiledeSoknaderService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

const val RETRY_TOPIC = "flex." + "sykepengesoknad-arkivering-oppgave-retry"

@Component
class RebehandleSykepengesoknadListener(
    private val behandleFeiledeSoknaderService: BehandleFeiledeSoknaderService,
    private val innsendingRepository: InnsendingRepository,
    private val rebebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer
) {
    val log = logger()

    @KafkaListener(
        topics = [RETRY_TOPIC],
        containerFactory = "aivenKafkaListenerContainerFactory",
        properties = [ "auto.offset.reset=earliest" ],
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
                acknowledgment.nack(Duration.ofMillis(sovetid))
            } else {
                val innsending = innsendingRepository.findBySykepengesoknadId(sykepengesoknad.id)
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

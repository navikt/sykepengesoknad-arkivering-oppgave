package no.nav.helse.import

import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.logger
import no.nav.helse.objectMapper
import no.nav.helse.repository.InnsendingDbRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import kotlin.system.measureTimeMillis

const val INNSENDING_MIGRERING_TOPIC = "flex." + "syfogsak-innsending-migrering"

@Component
class InnsendingImportListener(
    private val innsendingRepository: BatchInsertDAO,
    registry: MeterRegistry,
) {

    private val log = logger()
    val counter = registry.counter("mottatt_innsending_counter")

    @KafkaListener(
        topics = [INNSENDING_MIGRERING_TOPIC],
        containerFactory = "importKafkaListenerContainerFactory",
    )
    fun listen(records: List<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment) {

        val raderFraKafka = records
            .map { it.value().tilInnsendingKafkaDto() }
            .distinctBy { it.sykepengesoknadId }
            .map { it.tilInnsendingDbRecord() }

        if (raderFraKafka.isEmpty()) {
            acknowledgment.acknowledge()
            return
        }

        val elapsed = measureTimeMillis {
            innsendingRepository.batchInsertInnsending(raderFraKafka)
            counter.increment(raderFraKafka.size.toDouble())
        }
        log.info("Behandlet ${raderFraKafka.size} innsending rader fra kafka i $elapsed millis")

        acknowledgment.acknowledge()
    }
}

fun String.tilInnsendingKafkaDto(): InnsendingKafkaDto = objectMapper.readValue(this)

fun InnsendingKafkaDto.tilInnsendingDbRecord(): InnsendingDbRecord {
    return InnsendingDbRecord(
        id = null,
        sykepengesoknadId = sykepengesoknadId,
        journalpostId = journalpostId,
        oppgaveId = oppgaveId,
        behandlet = behandlet?.toInstant(),
    )
}

data class InnsendingKafkaDto(
    val sykepengesoknadId: String,
    val journalpostId: String? = null,
    val oppgaveId: String? = null,
    val behandlet: OffsetDateTime? = null,
)

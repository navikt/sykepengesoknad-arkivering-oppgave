package no.nav.syfo.import

import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.logger
import no.nav.syfo.objectMapper
import no.nav.syfo.repository.OppgaveStatus
import no.nav.syfo.repository.SpreOppgaveDbRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import kotlin.system.measureTimeMillis

const val OPPGAVESTYRING_MIGRERING_TOPIC = "flex." + "syfogsak-oppgavestyring-migrering"

@Component
class OppgavestyringImportListener(
    private val spreOppgaveRepository: BatchInsertDAO,
    registry: MeterRegistry,

) {

    private val log = logger()
    val counter = registry.counter("mottatt_oppgavestyring_counter")

    @KafkaListener(
        topics = [OPPGAVESTYRING_MIGRERING_TOPIC],
        containerFactory = "importKafkaListenerContainerFactory",
    )
    fun listen(records: List<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment) {

        val raderFraKafka = records
            .map { it.value().tilOppgavestyringKafkaDto() }
            .distinctBy { it.sykepengesoknadId }
            .map { it.tilSpreOppgaveDbRecord() }

        if (raderFraKafka.isEmpty()) {
            acknowledgment.acknowledge()
            return
        }

        val elapsed = measureTimeMillis {
            spreOppgaveRepository.batchInsertSpreOppgave(raderFraKafka)
            counter.increment(raderFraKafka.size.toDouble())
        }
        log.info("Behandlet ${raderFraKafka.size} oppgavestyring rader fra kafka i $elapsed millis")

        acknowledgment.acknowledge()
    }
}

fun String.tilOppgavestyringKafkaDto(): OppgavestyringKafkaDto = objectMapper.readValue(this)

fun OppgavestyringKafkaDto.tilSpreOppgaveDbRecord(): SpreOppgaveDbRecord {
    return SpreOppgaveDbRecord(
        id = null,
        sykepengesoknadId = sykepengesoknadId,
        status = status,
        opprettet = opprettet.toInstant(),
        modifisert = modifisert.toInstant(),
        timeout = timeout?.toInstant(),
        avstemt = avstemt
    )
}

data class OppgavestyringKafkaDto(
    val sykepengesoknadId: String,
    val timeout: OffsetDateTime?,
    val status: OppgaveStatus,
    val opprettet: OffsetDateTime,
    val modifisert: OffsetDateTime,
    val avstemt: Boolean
)

package no.nav.syfo.config

import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.SyfoProducerRecord
import no.nav.syfo.kafka.producer.RebehandlingProducer
import no.nav.syfo.skapConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
@Primary
class RebehandlingProducerMock : RebehandlingProducer {

    val topicMeldinger = ArrayList<SyfoProducerRecord<String, Sykepengesoknad>>()

    fun hentSisteSomConsumerRecord(): ConsumerRecord<String, Sykepengesoknad> {
        val sisteProducerRecord = topicMeldinger.last()
        return skapConsumerRecord(sisteProducerRecord.key(), sisteProducerRecord.value(), sisteProducerRecord.headers())
    }

    override fun leggPaRebehandlingTopic(sykepengesoknad: Sykepengesoknad, behandlingstidspunkt: LocalDateTime) {
        if (topicMeldinger.size >= 10) {
            throw RuntimeException("For mange rebehandlinger i testen, noe er feil satt opp")
        }

        val syfoProducerRecord = SyfoProducerRecord(
            "syfogsak-rebehandle-soknad-v1", sykepengesoknad.id, sykepengesoknad,
            mapOf(Pair(BEHANDLINGSTIDSPUNKT, LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        )

        topicMeldinger.add(syfoProducerRecord)
    }
}

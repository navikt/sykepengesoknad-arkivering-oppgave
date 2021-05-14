package no.nav.syfo.kafka.producer

import no.nav.syfo.BEHANDLINGSTIDSPUNKT
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.SyfoProducerRecord
import no.nav.syfo.logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Component
class RebehandlingProducerImpl @Inject
constructor(private val kafkaTemplate: KafkaTemplate<String, Sykepengesoknad>) : RebehandlingProducer {
    private val log = logger()

    override fun leggPaRebehandlingTopic(sykepengesoknad: Sykepengesoknad, behandlingstidspunkt: LocalDateTime) {
        try {
            kafkaTemplate.send(
                SyfoProducerRecord<String, Sykepengesoknad>(
                    "syfogsak-rebehandle-soknad-v1", sykepengesoknad.id, sykepengesoknad,
                    mapOf(Pair(BEHANDLINGSTIDSPUNKT, behandlingstidspunkt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                )
            ).get()
        } catch (exception: Exception) {
            log.error("Det feiler når søknad ${sykepengesoknad.id} skal legges på rebehandling-topic", exception)
            throw RuntimeException(exception)
        }
    }
}

interface RebehandlingProducer {
    fun leggPaRebehandlingTopic(sykepengesoknad: Sykepengesoknad, behandlingstidspunkt: LocalDateTime)
}

package no.nav.syfo.kafka

import no.nav.syfo.logger
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.kafka.listener.SeekToCurrentErrorHandler
import org.springframework.stereotype.Component
import org.springframework.util.backoff.ExponentialBackOff

@Component
class AivenKafkaErrorHandler : SeekToCurrentErrorHandler(
    null,
    ExponentialBackOff(1000L, 1.5).also {
        it.maxInterval = 60_000L * 10
    }
) {
    private val log = logger()

    override fun handle(
        thrownException: Exception,
        records: List<ConsumerRecord<*, *>>?,
        consumer: Consumer<*, *>?,
        container: MessageListenerContainer
    ) {

        records?.forEach { record ->
            log.error(
                "Feil i prossesseringen av record med offset: ${record.offset()}, key: ${record.key()} på topic ${record.topic()}",
                thrownException
            )
        }
        if (records == null || records.isEmpty()) {
            log.error("Feil i listener uten noen records", thrownException)
        }

        super.handle(thrownException, records, consumer, container)
    }
}

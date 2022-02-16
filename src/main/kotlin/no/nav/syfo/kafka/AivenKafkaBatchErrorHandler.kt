package no.nav.syfo.kafka

import no.nav.syfo.logger
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.kafka.listener.SeekToCurrentBatchErrorHandler
import org.springframework.stereotype.Component
import java.lang.Exception

@Component
class AivenKafkaBatchErrorHandler : SeekToCurrentBatchErrorHandler() {
    private val log = logger()

    override fun handle(
        thrownException: Exception,
        records: ConsumerRecords<*, *>?,
        consumer: Consumer<*, *>,
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

package no.nav.syfo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.ContainerStoppingErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class KafkaErrorHandler implements ContainerAwareErrorHandler {

    private static ContainerStoppingErrorHandler STOPPING_ERROR_HANDLER = new ContainerStoppingErrorHandler();

    @Override
    public void handle(Exception thrownException, List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer, MessageListenerContainer container) {
        records
                .forEach(record -> {
                    log.error("Det har skjedd en feil i prossesseringen av record med offset:{} og innhold:{}", record.offset(), record.value());
                });

        STOPPING_ERROR_HANDLER.handle(thrownException, records, consumer, container);
    }
}

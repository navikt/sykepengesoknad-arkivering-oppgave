package no.nav.syfo.consumer.kafka.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestListener {
    @KafkaListener(topics = "testtopic")
    public void listen(ConsumerRecord<Integer, String> cr) throws Exception {
        log.info("Mottatt melding med offset {}, id {} og value {}", cr.offset(), cr.key(), cr.value());
    }
}

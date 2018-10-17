package no.nav.syfo.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestProducer {
    private KafkaTemplate<String, SykepengesoknadDTO> kafkaTemplate;

    public TestProducer(KafkaTemplate<String, SykepengesoknadDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void soknadSendt(SykepengesoknadDTO sykepengesoknad) {
        kafkaTemplate.send(
                new SyfoProducerRecord<>("privat-syfo-soknadSendt-v1", sykepengesoknad.getId(), sykepengesoknad));
        log.info("Soknad med id: {}, er sendt", sykepengesoknad.getId());
    }
}

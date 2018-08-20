package no.nav.syfo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.util.UUID.randomUUID;

@Component
@Slf4j
public class TestProducer {
    private KafkaTemplate<String, String> kafkaTemplate;

    public TestProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void soknadSendt(Sykepengesoknad sykepengesoknad) {
        try {
            String serialisertSoknad = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .configure(WRITE_DATES_AS_TIMESTAMPS, false)
                    .writeValueAsString(sykepengesoknad);

            kafkaTemplate.send(
                    "aapen-syfo-soeknadSendt-v1",
                    randomUUID().toString(),
                    serialisertSoknad);
            log.info("Soknad med id: {}, er sendt", sykepengesoknad.getId());
        } catch (JsonProcessingException e) {
            log.error("Kunne ikke serialisere sykepengesøknad");
        }
    }
}

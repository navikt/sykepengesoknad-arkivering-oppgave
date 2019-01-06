package no.nav.syfo.config;

import no.nav.syfo.kafka.soknad.dto.SoknadDTO;
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableKafka
public class KafkaTestConfig {

    @Bean
    public ProducerFactory<String, SoknadDTO> producerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                new FunctionSerializer<>(soknadDTO -> new byte[]{1}));
    }

    @Bean
    public KafkaTemplate<String, SoknadDTO> kafkaTemplate(ProducerFactory<String, SoknadDTO> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}

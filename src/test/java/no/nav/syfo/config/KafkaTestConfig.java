package no.nav.syfo.config;

import no.nav.syfo.kafka.sykepengesoknad.deserializer.SykepengesoknadDeserializer;
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO;
import no.nav.syfo.kafka.sykepengesoknad.serializer.SykepengesoknadSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;

@Configuration
@EnableKafka
public class KafkaTestConfig {

    @Bean
    public ConsumerFactory<String, SykepengesoknadDTO> consumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(), new StringDeserializer(), new SykepengesoknadDeserializer());
    }

    @Deprecated
    @Bean
    public ConsumerFactory<String, String> deprecatedConsumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(), new StringDeserializer(), new StringDeserializer());
    }

    @Bean
    public ProducerFactory<String, SykepengesoknadDTO> producerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(), new StringSerializer(), new SykepengesoknadSerializer());
    }

    @Bean
    public KafkaTemplate<String, SykepengesoknadDTO> kafkaTemplate(ProducerFactory<String, SykepengesoknadDTO> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}

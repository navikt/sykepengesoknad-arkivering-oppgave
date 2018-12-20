package no.nav.syfo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.syfo.kafka.soknad.deserializer.MultiFunctionDeserializer;
import no.nav.syfo.kafka.soknad.dto.SoknadDTO;
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;

import java.io.IOException;

import static java.util.Collections.emptyMap;

@Configuration
@EnableKafka
public class KafkaTestConfig {

    @Bean
    public ConsumerFactory<String, SoknadDTO> consumerFactory(
            KafkaProperties kafkaProperties,
            ObjectMapper objectMapper
    ) {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(), new StringDeserializer(),
                new MultiFunctionDeserializer<>(emptyMap(), bytes -> {
                    try {
                        return objectMapper.readValue(bytes, SoknadDTO.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Feil ved konvertering av bytes til søknad", e);
                    }
                }));
    }

    @Deprecated
    @Bean
    public ConsumerFactory<String, String> deprecatedConsumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(), new StringDeserializer(), new StringDeserializer());
    }

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

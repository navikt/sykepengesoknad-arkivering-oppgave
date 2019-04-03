package no.nav.syfo.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.syfo.kafka.KafkaErrorHandler;
import no.nav.syfo.kafka.interfaces.Soknad;
import no.nav.syfo.kafka.soknad.deserializer.MultiFunctionDeserializer;
import no.nav.syfo.kafka.soknad.dto.SoknadDTO;
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Soknad> kafkaListenerContainerFactory(
            ConsumerFactory<String, Soknad> consumerFactory,
            KafkaErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Soknad> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setErrorHandler(kafkaErrorHandler);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Soknad> consumerFactory(
            KafkaProperties properties) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        Map<String, BiFunction<Headers, byte[], Soknad>> deserializere = new HashMap<>();
        BiFunction<Headers, byte[], Soknad> bytesToSykepengesoknadDTO = (headers, bytes) -> {
            try {
                return objectMapper.readValue(bytes, SykepengesoknadDTO.class);
            } catch (IOException e) {
                throw new RuntimeException("Feil ved konvertering av bytes til søknad", e);
            }
        };
        BiFunction<Headers, byte[], Soknad> bytesToSoknadDTO = (headers, bytes) -> {
            try {
                return objectMapper.readValue(bytes, SoknadDTO.class);
            } catch (IOException e) {
                throw new RuntimeException("Feil ved konvertering av bytes til søknad", e);
            }
        };
        deserializere.put("SYKEPENGESOKNAD", bytesToSykepengesoknadDTO);
        deserializere.put("SOKNAD", bytesToSoknadDTO);
        return new DefaultKafkaConsumerFactory<>(
                properties.buildConsumerProperties(),
                new StringDeserializer(),
                new MultiFunctionDeserializer<>(deserializere, (bytes) -> null));
    }
}



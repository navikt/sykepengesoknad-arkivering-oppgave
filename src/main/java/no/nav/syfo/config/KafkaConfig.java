package no.nav.syfo.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static java.util.Collections.*;
import static no.nav.syfo.kafka.KafkaHeaderConstants.MELDINGSTYPE;
import static no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString;

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
    @Profile(value = {"remote", "local-kafka"})
    @Primary
    public ConsumerFactory<String, Soknad> consumerFactory(
            KafkaProperties properties) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        Map<String, BiFunction<Headers, byte[], Soknad>> soknadmapper = new HashMap<>();
        BiFunction<Headers, byte[], Soknad> sykepengefunksjon = (headers, bytes) -> {
            try {
                return objectMapper.readValue(bytes, SykepengesoknadDTO.class);
            } catch (IOException e) {
                throw new RuntimeException("Feil ved konvertering av bytes til søknad", e);
            }
        },
                annenfunksjon = (headers, bytes) -> {
                    try {
                        return objectMapper.readValue(bytes, SoknadDTO.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Feil ved konvertering av bytes til søknad", e);
                    }
                };
        soknadmapper.put("SYKEPENGESOKNAD", sykepengefunksjon);
        soknadmapper.put("SELVSTENDIG", annenfunksjon);
        soknadmapper.put("UTENLANDS", annenfunksjon);
        return new DefaultKafkaConsumerFactory<>(
                properties.buildConsumerProperties(),
                new StringDeserializer(),
                new MultiFunctionDeserializer<>(soknadmapper, (bytes) -> null));
    }

    @Bean
    @Deprecated
    public ConcurrentKafkaListenerContainerFactory<String, SoknadDTO> deprecatedKafkaListenerContainerFactory(
            ConsumerFactory<String, SoknadDTO> deprecatedConsumerFactory,
            KafkaErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, SoknadDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setErrorHandler(kafkaErrorHandler);
        factory.setConsumerFactory(deprecatedConsumerFactory);
        return factory;
    }

    @Bean
    @Profile(value = {"remote", "local-kafka"})
    @Primary
    @Deprecated
    public ConsumerFactory<String, SoknadDTO> deprecatedConsumerFactory(
            KafkaProperties properties,
            ObjectMapper objectMapper) {
        return new DefaultKafkaConsumerFactory<>(
                properties.buildConsumerProperties(),
                new StringDeserializer(),
                new MultiFunctionDeserializer<>(emptyMap(),
                        bytes -> {
                            try {
                                return objectMapper.readValue(bytes, SoknadDTO.class);
                            } catch (IOException e) {
                                throw new RuntimeException("Feil ved konvertering av bytes til søknad", e);
                            }
                        }));
    }
}



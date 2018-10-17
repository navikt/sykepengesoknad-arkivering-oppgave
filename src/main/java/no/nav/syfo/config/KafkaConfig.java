package no.nav.syfo.config;

import no.nav.syfo.kafka.KafkaErrorHandler;
import no.nav.syfo.kafka.sykepengesoknad.deserializer.SykepengesoknadDeserializer;
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO;
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

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadDTO> kafkaListenerContainerFactory(
            ConsumerFactory<String, SykepengesoknadDTO> consumerFactory,
            KafkaErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setErrorHandler(kafkaErrorHandler);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    @Profile(value = {"remote", "local-kafka"})
    @Primary
    public ConsumerFactory<String, SykepengesoknadDTO> consumerFactory(KafkaProperties properties) {
        return new DefaultKafkaConsumerFactory<>(
                properties.buildConsumerProperties(),
                new StringDeserializer(),
                new SykepengesoknadDeserializer());
    }

    @Deprecated
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> deprecatedKafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory, KafkaErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setErrorHandler(kafkaErrorHandler);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Deprecated
    @Bean
    @Profile(value = {"remote", "local-kafka"})
    @Primary
    public ConsumerFactory<String, String> deprecatedConsumerFactory(KafkaProperties properties) {
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
    }
}



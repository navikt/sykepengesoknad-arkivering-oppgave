package no.nav.syfo.config;

import org.junit.ClassRule;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@Configuration
@EnableKafka
public class KafkaTestConfig {


    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "aapen-syfo-soeknadSendt-v1");

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(KafkaTestUtils.consumerProps("test", "false", embeddedKafka));
    }

    @Profile("remote")
    @Primary
    @Bean
    public ProducerFactory<String, String> producerFactory(KafkaProperties properties) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

}

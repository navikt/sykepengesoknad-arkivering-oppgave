package no.nav.syfo.config;

import org.junit.ClassRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@Configuration
@EnableKafka
public class KafkaTestConfig {

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "aapen-syfo-soeknadSendt-v1");

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(KafkaTestUtils.consumerProps("test", "false", embeddedKafka));
    }
}

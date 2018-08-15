package no.nav.syfo.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.web.client.RestTemplate;


@Configuration
@EnableTransactionManagement
@EnableKafka
@EnableScheduling
public class ApplicationConfig {
    // Sørger for at flyway migrering skjer etter at JTA transaction manager er ferdig satt opp av Spring.
    // Forhindrer WARNING: transaction manager not running? loggspam fra Atomikos.
    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy(final JtaTransactionManager jtaTransactionManager) {
        return Flyway::migrate;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().stream()
                .filter(AbstractJackson2HttpMessageConverter.class::isInstance)
                .map(AbstractJackson2HttpMessageConverter.class::cast)
                .map(AbstractJackson2HttpMessageConverter::getObjectMapper)
                .forEach(objectMapper -> objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false));
        return restTemplate;
    }
}



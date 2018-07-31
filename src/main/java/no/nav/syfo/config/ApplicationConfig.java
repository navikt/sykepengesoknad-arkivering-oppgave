package no.nav.syfo.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableTransactionManagement
@EnableKafka
public class ApplicationConfig {
    // Sørger for at flyway migrering skjer etter at JTA transaction manager er ferdig satt opp av Spring.
    // Forhindrer WARNING: transaction manager not running? loggspam fra Atomikos.
    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy(final JtaTransactionManager jtaTransactionManager) {
        return Flyway::migrate;
    }
}



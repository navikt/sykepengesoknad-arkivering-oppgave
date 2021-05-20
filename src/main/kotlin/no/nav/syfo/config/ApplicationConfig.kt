package no.nav.syfo.config

import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.jta.JtaTransactionManager
import org.springframework.web.client.RestTemplate

@Configuration
@EnableTransactionManagement
@EnableKafka
@EnableScheduling
class ApplicationConfig {
    /**
     * Sørger for at flyway migrering skjer etter at JTA transaction manager er ferdig satt opp av Spring.
     * Forhindrer> `WARNING: transaction manager not running? loggspam fra Atomikos`.
     */
    @Bean
    internal fun flywayMigrationStrategy(jtaTransactionManager: JtaTransactionManager): FlywayMigrationStrategy =
        FlywayMigrationStrategy { it.migrate() }

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate().apply {
        messageConverters
            .mapNotNull { it as? AbstractJackson2HttpMessageConverter }
            .forEach { it.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) }
    }
}

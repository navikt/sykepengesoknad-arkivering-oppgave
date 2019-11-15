package no.nav.syfo.config

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywayConfig {
    @Bean
    fun flyway(dataSource: DataSource) = Flyway().apply {
        setDataSource(dataSource)
        isBaselineOnMigrate = true
    }

    @Bean
    fun flywayMigrationInitializer(flyway: Flyway): FlywayMigrationInitializer =
        FlywayMigrationInitializer(flyway, null)
}

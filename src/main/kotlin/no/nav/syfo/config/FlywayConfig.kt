package no.nav.syfo.config

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywayConfig {
    @Bean
    fun flyway(dataSource: DataSource) = Flyway(Flyway.configure().apply {
        dataSource(dataSource)
        baselineOnMigrate(true)
    })

    @Bean
    fun flywayMigrationInitializer(flyway: Flyway): FlywayMigrationInitializer =
        FlywayMigrationInitializer(flyway, null)
}

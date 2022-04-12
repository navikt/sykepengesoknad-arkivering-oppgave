package no.nav.helse.flex

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

private class PostgreSQLContainer12 : PostgreSQLContainer<PostgreSQLContainer12>("postgres:12-alpine")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@EnableMockOAuth2Server
abstract class FellesTestoppsett {

    companion object {
        private val postgresContainer = PostgreSQLContainer12().apply {
            start()
            System.setProperty("spring.datasource.url", "$jdbcUrl&reWriteBatchedInserts=true")
            System.setProperty("spring.datasource.username", username)
            System.setProperty("spring.datasource.password", password)
        }

        private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.1.1")).apply {
            start()
            System.setProperty("KAFKA_BROKERS", bootstrapServers)
        }

        init {
            Runtime.getRuntime().addShutdownHook(
                Thread {
                    println("Avslutter testcontainers")
                    postgresContainer.close()
                    kafkaContainer.close()
                }
            )
        }
    }

    @Autowired
    lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate
}

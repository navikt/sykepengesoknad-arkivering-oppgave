package no.nav.syfo.config

import no.nav.syfo.TestApplication
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.test.annotation.DirtiesContext

@EmbeddedKafka
@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
@EnableScheduling
class ApplicationConfigTest {
    @Test
    fun test() {
    }
}

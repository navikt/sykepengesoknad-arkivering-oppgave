package no.nav.syfo.config

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
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
@EnableMockOAuth2Server
class ApplicationConfigTest {
    @Test
    fun test() {
    }
}

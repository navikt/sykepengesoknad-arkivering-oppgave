package no.nav.helse.flex

import io.getunleash.FakeUnleash
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.kafka.consumer.AivenSoknadSendtListener
import no.nav.helse.flex.kafka.consumer.AivenSpreOppgaverListener
import no.nav.helse.flex.medlemskap.MedlemskapVurderingRepository
import no.nav.helse.flex.mockdispatcher.*
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.service.OppgaveOpprettelse
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import okhttp3.mockwebserver.MockWebServer
import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.kafka.support.Acknowledgment
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private class PostgreSQLContainer12 : PostgreSQLContainer<PostgreSQLContainer12>("postgres:12-alpine")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@EnableMockOAuth2Server
abstract class FellesTestoppsett {

    companion object {
        val pdlMockWebserver: MockWebServer
        val pdfMockWebserver: MockWebServer
        val dokArkivMockWebserver: MockWebServer
        val oppgaveMockWebserver: MockWebServer
        val sykepengesoknadMockWebserver: MockWebServer
        val kvitteringMockWebserver: MockWebServer
        val medlemskapMockWebserver: MockWebServer

        init {
            val threads = mutableListOf<Thread>()

            thread {
                PostgreSQLContainer12().apply {
                    withCommand("postgres", "-c", "wal_level=logical")
                    start()
                    System.setProperty("spring.datasource.url", "$jdbcUrl&reWriteBatchedInserts=true")
                    System.setProperty("spring.datasource.username", username)
                    System.setProperty("spring.datasource.password", password)
                }
            }.also { threads.add(it) }

            thread {
                KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.1")).apply {
                    start()
                    System.setProperty("KAFKA_BROKERS", bootstrapServers)
                }
            }.also { threads.add(it) }

            pdlMockWebserver = MockWebServer().apply {
                System.setProperty("PDL_URL", "http://localhost:$port")
                dispatcher = PdlMockDispatcher
            }

            pdfMockWebserver = MockWebServer().apply {
                System.setProperty("PDFGEN_URL", "http://localhost:$port")
                dispatcher = PdfMockDispatcher
            }

            dokArkivMockWebserver = MockWebServer().apply {
                System.setProperty("DOKARKIV_URL", "http://localhost:$port")
                dispatcher = DokArkivMockDispatcher
            }

            oppgaveMockWebserver = MockWebServer().apply {
                System.setProperty("OPPGAVE_URL", "http://localhost:$port")
                dispatcher = OppgaveMockDispatcher
            }

            sykepengesoknadMockWebserver = MockWebServer().apply {
                System.setProperty("SYKEPENGESOKNAD_BACKEND_URL", "http://localhost:$port")
                dispatcher = SykepengesoknadMockDispatcher
            }

            kvitteringMockWebserver = MockWebServer().apply {
                System.setProperty("SYKEPENGESOKNAD_KVITTERINGER_URL", "http://localhost:$port")
                dispatcher = KvitteringMockDispatcher
            }

            medlemskapMockWebserver = MockWebServer().apply {
                System.setProperty("MEDLEMSKAP_VURDERING_URL", "http://localhost:$port")
                dispatcher = MedlemskapMockDispatcher
            }

            threads.forEach { it.join() }
        }
    }

    @Autowired
    lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    lateinit var fakeUnleash: FakeUnleash

    @Autowired
    lateinit var aivenSoknadSendtListener: AivenSoknadSendtListener

    @Autowired
    lateinit var aivenSpreOppgaverListener: AivenSpreOppgaverListener

    @MockBean
    lateinit var acknowledgment: Acknowledgment

    @Autowired
    lateinit var oppgaveOpprettelse: OppgaveOpprettelse

    @Autowired
    lateinit var innsendingRepository: InnsendingRepository

    @Autowired
    lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @Autowired
    lateinit var medlemskapVurderingRepository: MedlemskapVurderingRepository

    @AfterAll
    fun `Disable unleash toggles`() {
        fakeUnleash.disableAll()
    }

    @AfterAll
    fun `Rydd opp i databasen`() {
        innsendingRepository.deleteAll()
        spreOppgaveRepository.deleteAll()
        medlemskapVurderingRepository.deleteAll()
    }

    @AfterAll
    fun hentMockRequests() {
        while (pdlMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { /* ok */ }
        while (pdfMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { /* ok */ }
        while (dokArkivMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { /* ok */ }
        while (oppgaveMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { /* ok */ }
        while (sykepengesoknadMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { /* ok */ }
        while (kvitteringMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { /* ok */ }
        while (medlemskapMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { /* ok */ }
    }

    fun leggSøknadPåKafka(søknad: SykepengesoknadDTO) =
        aivenSoknadSendtListener.listen(skapConsumerRecord("key", søknad.serialisertTilString()), acknowledgment)

    fun leggOppgavePåAivenKafka(oppgave: OppgaveDTO) =
        aivenSpreOppgaverListener.listen(skapConsumerRecord("key", oppgave.serialisertTilString()), acknowledgment)
}

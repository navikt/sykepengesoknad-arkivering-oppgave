package no.nav.helse.flex

import io.getunleash.FakeUnleash
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.kafka.consumer.AivenSoknadSendtListener
import no.nav.helse.flex.kafka.consumer.AivenSpreOppgaverListener
import no.nav.helse.flex.medlemskap.MedlemskapVurderingRepository
import no.nav.helse.flex.mockdispatcher.DokArkivMockDispatcher
import no.nav.helse.flex.mockdispatcher.KvitteringMockDispatcher
import no.nav.helse.flex.mockdispatcher.MedlemskapMockDispatcher
import no.nav.helse.flex.mockdispatcher.OppgaveMockDispatcher
import no.nav.helse.flex.mockdispatcher.PdfMockDispatcher
import no.nav.helse.flex.mockdispatcher.PdlMockDispatcher
import no.nav.helse.flex.mockdispatcher.SykepengesoknadMockDispatcher
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.service.OppgaveOpprettelse
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.helse.flex.tilbakedaterte.OppgaverForTilbakedaterteRepository
import no.nav.helse.flex.tilbakedaterte.SykmeldingSendtBekreftetConsumer
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.sykmelding.kafka.model.SykmeldingKafkaMessage
import okhttp3.mockwebserver.MockWebServer
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

private class PostgreSQLContainer14 : PostgreSQLContainer<PostgreSQLContainer14>("postgres:14-alpine")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@EnableMockOAuth2Server
abstract class FellesTestOppsett {
    companion object {
        init {
            PostgreSQLContainer14().apply {
                withCommand("postgres", "-c", "wal_level=logical")
                start()
                System.setProperty("spring.datasource.url", "$jdbcUrl&reWriteBatchedInserts=true")
                System.setProperty("spring.datasource.username", username)
                System.setProperty("spring.datasource.password", password)
            }

            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1")).apply {
                start()
                System.setProperty("KAFKA_BROKERS", bootstrapServers)
            }
        }

        val pdlMockWebserver =
            MockWebServer().apply {
                System.setProperty("PDL_URL", "http://localhost:$port")
                dispatcher = PdlMockDispatcher
            }

        val pdfMockWebserver =
            MockWebServer().apply {
                System.setProperty("PDFGEN_URL", "http://localhost:$port")
                dispatcher = PdfMockDispatcher
            }

        val dokArkivMockWebserver =
            MockWebServer().apply {
                System.setProperty("DOKARKIV_URL", "http://localhost:$port")
                dispatcher = DokArkivMockDispatcher
            }

        val oppgaveMockWebserver =
            MockWebServer().apply {
                System.setProperty("OPPGAVE_URL", "http://localhost:$port")
                dispatcher = OppgaveMockDispatcher
            }

        val sykepengesoknadMockWebserver =
            MockWebServer().apply {
                System.setProperty("SYKEPENGESOKNAD_BACKEND_URL", "http://localhost:$port")
                dispatcher = SykepengesoknadMockDispatcher
            }

        val kvitteringMockWebserver =
            MockWebServer().apply {
                System.setProperty("SYKEPENGESOKNAD_KVITTERINGER_URL", "http://localhost:$port")
                dispatcher = KvitteringMockDispatcher
            }

        val medlemskapMockWebserver =
            MockWebServer().apply {
                System.setProperty("MEDLEMSKAP_VURDERING_URL", "http://localhost:$port")
                dispatcher = MedlemskapMockDispatcher
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

    @Autowired
    lateinit var sykmeldingSendtBekreftetConsumer: SykmeldingSendtBekreftetConsumer

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

    @Autowired
    lateinit var oppgaverForTilbakedaterteRepository: OppgaverForTilbakedaterteRepository

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
        while (pdlMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { // ok
        }
        while (pdfMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { // ok
        }
        while (dokArkivMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { // ok
        }
        while (oppgaveMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { // ok
        }
        while (sykepengesoknadMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { // ok
        }
        while (kvitteringMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { // ok
        }
        while (medlemskapMockWebserver.takeRequest(1, TimeUnit.MILLISECONDS) != null) { // ok
        }
    }

    fun leggSøknadPåKafka(søknad: SykepengesoknadDTO) =
        aivenSoknadSendtListener.listen(skapConsumerRecord("key", søknad.serialisertTilString()), acknowledgment)

    fun leggOppgavePåAivenKafka(oppgave: OppgaveDTO) =
        aivenSpreOppgaverListener.listen(skapConsumerRecord("key", oppgave.serialisertTilString()), acknowledgment)

    fun leggSykmeldingPåKafka(sykmelding: SykmeldingKafkaMessage) =
        sykmeldingSendtBekreftetConsumer.listen(
            skapConsumerRecord(sykmelding.sykmelding.id, sykmelding.serialisertTilString()),
            acknowledgment,
        )
}

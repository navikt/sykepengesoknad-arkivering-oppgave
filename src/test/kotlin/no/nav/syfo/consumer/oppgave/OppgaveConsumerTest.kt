package no.nav.syfo.consumer.oppgave

import no.nav.syfo.consumer.token.Token
import no.nav.syfo.consumer.token.TokenConsumer
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Soknadstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.DayOfWeek.*
import java.time.LocalDate.now
import java.time.temporal.TemporalAdjusters.next
import javax.inject.Inject

@RunWith(MockitoJUnitRunner::class)
class OppgaveConsumerTest {
    private val aktorId = "aktorId"
    private val behandlendeEnhet = "0101"
    private val saksId = "123"
    private val journalpostId = "145"

    @Mock
    lateinit var tokenConsumer: TokenConsumer
    @Mock
    lateinit var restTemplate: RestTemplate

    @Inject
    private lateinit var oppgaveConsumer: OppgaveConsumer

    @Before
    fun setup() {
        oppgaveConsumer = OppgaveConsumer(
                tokenConsumer = tokenConsumer,
                username = "username",
                url = "https://oppgave.nav.no",
                restTemplate = restTemplate)
        BDDMockito.given(tokenConsumer.token).willReturn(Token("token", "Bearer", 3600))
    }

    @Test
    fun innsendingLordagOgSondagGirSammeFristSomMandag() {
        assertThat(oppgaveConsumer.omTreUkedager(now().with(next(SATURDAY))).dayOfWeek).isEqualTo(THURSDAY)
        assertThat(oppgaveConsumer.omTreUkedager(now().with(next(SUNDAY))).dayOfWeek).isEqualTo(THURSDAY)
        assertThat(oppgaveConsumer.omTreUkedager(now().with(next(MONDAY))).dayOfWeek).isEqualTo(THURSDAY)
    }

    @Test
    fun fristSettesOmTreDagerUtenomHelg() {
        assertThat(oppgaveConsumer.omTreUkedager(now().with(next(MONDAY))).dayOfWeek).isEqualTo(THURSDAY)
        assertThat(oppgaveConsumer.omTreUkedager(now().with(next(TUESDAY))).dayOfWeek).isEqualTo(FRIDAY)
    }

    @Test
    fun toDagerLeggesTilOverHelg() {
        assertThat(oppgaveConsumer.omTreUkedager(now().with(next(WEDNESDAY))).dayOfWeek).isEqualTo(MONDAY)
        assertThat(oppgaveConsumer.omTreUkedager(now().with(next(THURSDAY))).dayOfWeek).isEqualTo(TUESDAY)
        assertThat(oppgaveConsumer.omTreUkedager(now().with(next(FRIDAY))).dayOfWeek).isEqualTo(WEDNESDAY)
    }

    @Test
    fun opprettSakOppretterSakOgReturnererSakId() {
        val response = OppgaveResponse(1234)

        BDDMockito.given(restTemplate.exchange(
                BDDMockito.anyString(),
                BDDMockito.any(HttpMethod::class.java),
                BDDMockito.any(HttpEntity::class.java),
                BDDMockito.eq(OppgaveResponse::class.java)
        )).willReturn(ResponseEntity(response, HttpStatus.CREATED))

        val oppgaveId = oppgaveConsumer.opprettOppgave(aktorId, behandlendeEnhet, saksId, journalpostId, lagSoknad(Soknadstype.ARBEIDSTAKERE))

        assertThat(oppgaveId).isEqualTo("1234")
    }

    @Test(expected = RuntimeException::class)
    fun opprettOppgaveGirFeilmeldingHvisOppgaveErNede() {
        BDDMockito.given(restTemplate.exchange(
                BDDMockito.anyString(),
                BDDMockito.any(HttpMethod::class.java),
                BDDMockito.any(HttpEntity::class.java),
                BDDMockito.eq(OppgaveResponse::class.java)
        )).willReturn(ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE))

        oppgaveConsumer.opprettOppgave(aktorId, behandlendeEnhet, saksId, journalpostId, lagSoknad(Soknadstype.ARBEIDSTAKERE))
    }

    @Test
    fun lagRequestHeadersHarMedPaakrevdCorrelationId() {
        val headers = oppgaveConsumer.lagRequestHeaders()

        assertThat(headers["X-Correlation-ID"]).isNotEmpty
    }

    @Test
    fun lagRequestBodyLagerRequestMedRiktigeFelter() {
        val body = oppgaveConsumer.lagRequestBody(aktorId, behandlendeEnhet, saksId, journalpostId, lagSoknad(Soknadstype.ARBEIDSTAKERE))

        assertThat(body.tildeltEnhetsnr).isEqualTo(behandlendeEnhet)
        assertThat(body.opprettetAvEnhetsnr).isEqualTo("9999")
        assertThat(body.aktoerId).isEqualTo(aktorId)
        assertThat(body.journalpostId).isEqualTo(journalpostId)
        assertThat(body.saksreferanse).isEqualTo(saksId)
        assertThat(body.beskrivelse).isNotEmpty()
        assertThat(body.tema).isEqualTo("SYK")
        assertThat(body.behandlingstema).isEqualTo("ab0061")
        assertThat(body.oppgavetype).isEqualTo("SOK")
        assertThat(body.aktivDato).isNotEmpty()
        assertThat(body.fristFerdigstillelse).isNotEmpty()
        assertThat(body.prioritet).isEqualTo("NORM")
    }

    @Test
    fun lagRequestBodySetterRiktigBehandlingstemaForUtland() {
        val body = oppgaveConsumer.lagRequestBody(aktorId, behandlendeEnhet, saksId, journalpostId, lagSoknad(Soknadstype.OPPHOLD_UTLAND))

        assertThat(body.behandlingstema).isEqualTo("ab0314")
    }

    private fun lagSoknad(soknadstype: Soknadstype): Soknad {
        return Soknad(
                aktorId,
                "",
                "fnr",
                "Navn",
                soknadstype,
                now().minusWeeks(3),
                now().minusDays(3),
                null,
                null,
                now().minusWeeks(3),
                now().minusWeeks(3),
                "arbeidsgiver",
                null,
                null,
                null,
                ArrayList(),
                ArrayList()
        )
    }
}


package no.nav.syfo.service

import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.token.Token
import no.nav.syfo.token.TokenConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate.now
import java.time.temporal.TemporalAdjusters.next

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OppgaveServiceTest {
    private val aktorId = "aktorId"
    private val journalpostId = "145"

    @Mock
    lateinit var tokenConsumer: TokenConsumer
    @Mock
    lateinit var restTemplate: RestTemplate
    @Mock
    private lateinit var oppgaveService: OppgaveService

    @BeforeEach
    fun setup() {
        oppgaveService = OppgaveService(
            tokenConsumer = tokenConsumer,
            username = "username",
            url = "https://oppgave.nav.no",
            restTemplate = restTemplate
        )
        BDDMockito.given(tokenConsumer.token).willReturn(Token("token", "Bearer", 3600))
    }

    @Test
    fun innsendingLordagOgSondagGirSammeFristSomMandag() {
        assertThat(OppgaveService.omTreUkedager(now().with(next(SATURDAY))).dayOfWeek).isEqualTo(THURSDAY)
        assertThat(OppgaveService.omTreUkedager(now().with(next(SUNDAY))).dayOfWeek).isEqualTo(THURSDAY)
        assertThat(OppgaveService.omTreUkedager(now().with(next(MONDAY))).dayOfWeek).isEqualTo(THURSDAY)
    }

    @Test
    fun fristSettesOmTreDagerUtenomHelg() {
        assertThat(OppgaveService.omTreUkedager(now().with(next(MONDAY))).dayOfWeek).isEqualTo(THURSDAY)
        assertThat(OppgaveService.omTreUkedager(now().with(next(TUESDAY))).dayOfWeek).isEqualTo(FRIDAY)
    }

    @Test
    fun toDagerLeggesTilOverHelg() {
        assertThat(OppgaveService.omTreUkedager(now().with(next(WEDNESDAY))).dayOfWeek).isEqualTo(MONDAY)
        assertThat(OppgaveService.omTreUkedager(now().with(next(THURSDAY))).dayOfWeek).isEqualTo(TUESDAY)
        assertThat(OppgaveService.omTreUkedager(now().with(next(FRIDAY))).dayOfWeek).isEqualTo(WEDNESDAY)
    }

    @Test
    fun opprettOppgaveGirFeilmeldingHvisOppgaveErNede() {
        assertThrows(RuntimeException::class.java) {
            BDDMockito.given(
                restTemplate.exchange(
                    BDDMockito.anyString(),
                    BDDMockito.any(HttpMethod::class.java),
                    BDDMockito.any(HttpEntity::class.java),
                    BDDMockito.eq(OppgaveResponse::class.java)
                )
            ).willReturn(ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE))

            val oppgaveRequest = OppgaveService.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.ARBEIDSTAKERE))
            oppgaveService.opprettOppgave(oppgaveRequest)
        }
    }

    @Test
    fun lagRequestHeadersHarMedPaakrevdCorrelationId() {
        val headers = oppgaveService.lagRequestHeaders()

        assertThat(headers["X-Correlation-ID"]).isNotEmpty
    }

    @Test
    fun lagRequestBodyLagerRequestMedRiktigeFelter() {
        val body = OppgaveService.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.ARBEIDSTAKERE))

        assertThat(body.tildeltEnhetsnr).isEqualTo(null)
        assertThat(body.opprettetAvEnhetsnr).isEqualTo("9999")
        assertThat(body.aktoerId).isEqualTo(aktorId)
        assertThat(body.journalpostId).isEqualTo(journalpostId)
        assertThat(body.beskrivelse).isNotEmpty()
        assertThat(body.tema).isEqualTo("SYK")
        assertThat(body.behandlingstema).isEqualTo("ab0061")
        assertThat(body.oppgavetype).isEqualTo("SOK")
        assertThat(body.aktivDato).isNotEmpty()
        assertThat(body.fristFerdigstillelse).isNotEmpty()
        assertThat(body.prioritet).isEqualTo("NORM")
    }

    @Test
    fun lagRequestBodySetterRiktigBehandlingstema() {
        val utland = OppgaveService.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.OPPHOLD_UTLAND))
        val arbeidstaker = OppgaveService.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.ARBEIDSTAKERE))
        val arbeidsledig = OppgaveService.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.ARBEIDSLEDIG))
        val behandlingsdager = OppgaveService.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.BEHANDLINGSDAGER))
        val redusertVenteperiode = OppgaveService.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.SELVSTENDIGE_OG_FRILANSERE), harRedusertVenteperiode = true)

        assertThat(utland.behandlingstema).isEqualTo("ab0314")
        assertThat(arbeidstaker.behandlingstema).isEqualTo("ab0061")
        assertThat(arbeidsledig.behandlingstema).isEqualTo("ab0426")
        assertThat(behandlingsdager.behandlingstema).isEqualTo("ab0351")
        assertThat(redusertVenteperiode.behandlingstype).isEqualTo("ae0247")
    }

    private fun lagSoknad(soknadstype: Soknadstype): Soknad {
        return Soknad(
            aktorId = aktorId,
            soknadsId = "",
            fnr = "fnr",
            navn = "Navn",
            tilNav = true,
            soknadstype = soknadstype,
            fom = now().minusWeeks(3),
            tom = now().minusDays(3),
            innsendtDato = null,
            sendtArbeidsgiver = null,
            startSykeforlop = now().minusWeeks(3),
            sykmeldingUtskrevet = now().minusWeeks(3),
            arbeidsgiver = "arbeidsgiver",
            korrigerer = null,
            korrigertAv = null,
            arbeidssituasjon = null,
            soknadPerioder = ArrayList(),
            sporsmal = ArrayList(),
            avsendertype = null,
            merknaderFraSykmelding = null
        )
    }
}

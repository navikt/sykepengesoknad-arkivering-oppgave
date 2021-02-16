package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.TestApplication
import no.nav.syfo.any
import no.nav.syfo.consumer.aktor.AktorConsumer
import no.nav.syfo.consumer.oppgave.OppgaveConsumer
import no.nav.syfo.consumer.oppgave.OppgaveRequest
import no.nav.syfo.consumer.oppgave.OppgaveResponse
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.repository.TidligereInnsending
import no.nav.syfo.consumer.sak.SakConsumer
import no.nav.syfo.consumer.ws.BehandleJournalConsumer
import no.nav.syfo.consumer.ws.PersonConsumer
import no.nav.syfo.domain.Innsending
import no.nav.syfo.domain.dto.Soknadstype.ARBEIDSTAKERE
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.producer.RebehandlingProducer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SaksbehandlingsServiceTest {

    @Mock
    lateinit var aktorConsumer: AktorConsumer
    @Mock
    lateinit var personConsumer: PersonConsumer
    @Mock
    lateinit var innsendingDAO: InnsendingDAO
    @Mock
    lateinit var sakConsumer: SakConsumer
    @Mock
    lateinit var oppgaveConsumer: OppgaveConsumer
    @Mock
    lateinit var behandleJournalConsumer: BehandleJournalConsumer
    @Mock
    lateinit var behandlendeEnhetService: BehandlendeEnhetService
    @Mock
    lateinit var registry: MeterRegistry
    @Mock
    lateinit var rebehandlingProducer: RebehandlingProducer

    @InjectMocks
    lateinit var saksbehandlingsService: SaksbehandlingsService

    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), KotlinModule())
    private val aktorId = "aktorId-745463060"

    @BeforeEach
    fun setup() {
        given(aktorConsumer.finnFnr(any())).willReturn("12345678901")
        given(personConsumer.finnBrukerPersonnavnByFnr(any())).willReturn("Personnavn")
        given(sakConsumer.opprettSak(any())).willReturn("ny-sak-fra-gsak")
        given(behandleJournalConsumer.opprettJournalpost(any(), any())).willReturn("journalpostId")
        given(behandlendeEnhetService.hentBehandlendeEnhet("12345678901", ARBEIDSTAKERE)).willReturn("2017")
        given(oppgaveConsumer.opprettOppgave(any())).willReturn(OppgaveResponse(1234))
        given(registry.counter(ArgumentMatchers.anyString(), ArgumentMatchers.anyIterable())).willReturn(mock(Counter::class.java))
        given(innsendingDAO.opprettInnsending(any(), any(), any(), any())).willReturn("innsending-guid")
    }

    @Test
    fun behandlerSoknaderSomEttersendesTilNavDerDetManglerOppgave() {
        val now = LocalDateTime.now()
        val sykepengesoknadUtenOppgave = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
            .copy(sendtNav = null, sendtArbeidsgiver = now)
        val sykepengesoknadEttersendingTilNAV = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
            .copy(sendtNav = now, sendtArbeidsgiver = now)

        saksbehandlingsService.behandleSoknad(sykepengesoknadUtenOppgave)
        verify(behandleJournalConsumer, times(1)).opprettJournalpost(any(), any())
        verify(oppgaveConsumer, never()).opprettOppgave(any())
        given(innsendingDAO.finnInnsendingForSykepengesoknad(sykepengesoknadUtenOppgave.id)).willReturn(
            innsending(sykepengesoknadUtenOppgave)
        )

        saksbehandlingsService.behandleSoknad(sykepengesoknadEttersendingTilNAV)
        saksbehandlingsService.opprettOppgave(sykepengesoknadEttersendingTilNAV, innsending(sykepengesoknadEttersendingTilNAV))
        verify(behandleJournalConsumer, times(1)).opprettJournalpost(any(), any())
        verify(oppgaveConsumer, times(1)).opprettOppgave(any())
    }

    private fun innsending(
        sykepengesoknadUtenOppgave: Sykepengesoknad
    ): Innsending {
        return Innsending(
            innsendingsId = "innsending-guid",
            ressursId = sykepengesoknadUtenOppgave.id,
            aktorId = sykepengesoknadUtenOppgave.aktorId,
            saksId = "ny-sak-fra-gsak",
            journalpostId = "journalpostId",
            oppgaveId = null,
            behandlet = LocalDate.now(),
            soknadFom = sykepengesoknadUtenOppgave.fom,
            soknadTom = sykepengesoknadUtenOppgave.tom
        )
    }

    @Test
    fun brukerEksisterendeSakOmSoknadErPafolgende() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
            .copy(fom = LocalDate.of(2019, 3, 11), tom = LocalDate.of(2019, 3, 20))

        given(innsendingDAO.finnTidligereInnsendinger(aktorId)).willReturn(listOf(TidligereInnsending(aktorId, "sak1", LocalDate.now(), LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 10))))
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer, never()).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "sak1")
    }

    @Test
    fun brukerIkkeEksisterendeSakOmSoknadIkkeErPafolgende() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
            .copy(fom = LocalDate.of(2019, 3, 11), tom = LocalDate.of(2019, 3, 20))

        given(innsendingDAO.finnTidligereInnsendinger(aktorId)).willReturn(listOf(TidligereInnsending(aktorId, "sak1", LocalDate.now(), LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 6))))
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "ny-sak-fra-gsak")
    }

    @Test
    fun brukerEksisterendeSakOmSoknadErPafolgendeMedHelg() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
            .copy(fom = LocalDate.of(2019, 3, 11), tom = LocalDate.of(2019, 3, 20))

        given(innsendingDAO.finnTidligereInnsendinger(aktorId)).willReturn(listOf(TidligereInnsending(aktorId, "sak1", LocalDate.now(), LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 8))))
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer, never()).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "sak1")
    }

    @Test
    fun brukerIkkeEksisterendeSakOmSoknadInnenforToDagerMenIkkeHelg() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
            .copy(fom = LocalDate.of(2019, 3, 12), tom = LocalDate.of(2019, 3, 21))

        given(innsendingDAO.finnTidligereInnsendinger(aktorId)).willReturn(listOf(TidligereInnsending(aktorId, "sak1", LocalDate.now(), LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 10))))
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "ny-sak-fra-gsak")
    }

    @Test
    fun brukerIkkeEksisterendeSakOmViIkkeHarTidligereInnsending() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
            .copy(fom = LocalDate.of(2019, 3, 12), tom = LocalDate.of(2019, 3, 21))

        given(innsendingDAO.finnTidligereInnsendinger(aktorId)).willReturn(emptyList())
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "ny-sak-fra-gsak")
    }

    @Test
    fun brukerIkkeEksistrendeSakOmInnsendingErEtterSoknad() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
            .copy(fom = LocalDate.of(2019, 2, 11), tom = LocalDate.of(2019, 2, 21))

        given(innsendingDAO.finnTidligereInnsendinger(aktorId)).willReturn(listOf(TidligereInnsending(aktorId, "sak1", LocalDate.now(), LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 10))))
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "ny-sak-fra-gsak")
    }

    @Test
    fun brukerEksisterendeSakOmSoknadErPafolgendeMedHelgFlereInnsendinger() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
            .copy(fom = LocalDate.of(2019, 3, 11), tom = LocalDate.of(2019, 3, 20))

        given(innsendingDAO.finnTidligereInnsendinger(aktorId)).willReturn(
            listOf(
                TidligereInnsending(aktorId, "sak1", LocalDate.now(), LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 8)),
                TidligereInnsending(aktorId, "sak2", LocalDate.now(), LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 8)),
                TidligereInnsending(aktorId, "sak3", LocalDate.now(), LocalDate.of(2018, 3, 1), LocalDate.of(2018, 3, 8))
            )
        )
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer, never()).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "sak2")
    }

    @Test
    fun `oppretter oppgave med korrekte felter`() {
        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        val søknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
        saksbehandlingsService.opprettOppgave(søknad, innsending(søknad))
        verify(oppgaveConsumer).opprettOppgave(captor.capture())
        val oppgaveRequest = captor.firstValue
        Assertions.assertThat(oppgaveRequest.aktoerId).isEqualTo(aktorId)
        Assertions.assertThat(oppgaveRequest.journalpostId).isEqualTo("journalpostId")
        Assertions.assertThat(oppgaveRequest.saksreferanse).isEqualTo("ny-sak-fra-gsak")
        Assertions.assertThat(oppgaveRequest.beskrivelse).isEqualTo(
            """
Søknad om sykepenger for perioden 16.10.2018 - 24.10.2018

Arbeidsgiver: ARBEIDSGIVER A/S
Organisasjonsnummer: 1257358

Periode 1:
16.10.2018 - 20.10.2018
Grad: 100

Periode 2:
21.10.2018 - 24.10.2018
Grad: 40

Betaler arbeidsgiveren lønnen din når du er syk?
Vet ikke
            """.trimIndent()
        )
        Assertions.assertThat(oppgaveRequest.tema).isEqualTo("SYK")
        Assertions.assertThat(oppgaveRequest.oppgavetype).isEqualTo("SOK")
        Assertions.assertThat(oppgaveRequest.prioritet).isEqualTo("NORM")
        Assertions.assertThat(oppgaveRequest.behandlingstema).isEqualTo("ab0061")
    }
}

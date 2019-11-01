package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.TestApplication
import no.nav.syfo.any
import no.nav.syfo.consumer.aktor.AktorConsumer
import no.nav.syfo.consumer.oppgave.OppgaveConsumer
import no.nav.syfo.consumer.oppgave.OppgaveResponse
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.repository.TidligereInnsending
import no.nav.syfo.consumer.sak.SakConsumer
import no.nav.syfo.consumer.ws.BehandleJournalConsumer
import no.nav.syfo.consumer.ws.PersonConsumer
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Soknadstype.ARBEIDSTAKERE
import no.nav.syfo.domain.dto.Soknadstype.SELVSTENDIGE_OG_FRILANSERE
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.producer.RebehandlingProducer
import no.nav.syfo.oppgave.UtsattOppgave
import no.nav.syfo.oppgave.UtsattOppgaveDAO
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
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
    @Mock
    lateinit var utsattOppgaveDAO: UtsattOppgaveDAO

    @InjectMocks
    lateinit var saksbehandlingsService: SaksbehandlingsService

    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), KotlinModule())

    @Before
    fun setup() {
        val defaultSykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
        val defaultSoknad = Soknad.lagSoknad(defaultSykepengesoknad, "12345678901", "Personnavn")
        given(aktorConsumer.finnFnr(any())).willReturn("12345678901")
        given(personConsumer.finnBrukerPersonnavnByFnr(any())).willReturn("Personnavn")
        given(sakConsumer.opprettSak(any())).willReturn("ny-sak-fra-gsak")
        given(behandleJournalConsumer.opprettJournalpost(any(), any())).willReturn("journalpostId")
        given(behandlendeEnhetService.hentBehandlendeEnhet("12345678901", SELVSTENDIGE_OG_FRILANSERE)).willReturn("2017")
        given(behandlendeEnhetService.hentBehandlendeEnhet("12345678901", ARBEIDSTAKERE)).willReturn("2017")
        given(oppgaveConsumer.opprettOppgave(any())).willReturn(OppgaveResponse(1234))
        given<Counter>(registry.counter(ArgumentMatchers.anyString(), ArgumentMatchers.anyIterable())).willReturn(mock(Counter::class.java))
        given(innsendingDAO.opprettInnsending(any(), any(), any(), any())).willReturn("innsending-guid")
        given(utsattOppgaveDAO.hentUtsattOppgaverForAktorId(any())).willReturn(listOf(UtsattOppgave("innsending-guid", OppgaveConsumer.lagRequestBody("aktorId-745463060", "2017", "ny-sak-fra-gsak", "journalpostId", defaultSoknad))))
    }

    @Test
    fun behandlerIkkeIkkeSendteSoknader() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(status = "NY")

        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(innsendingDAO, never()).finnInnsendingForSykepengesoknad(any())
    }

    @Test
    fun behandlerIkkeSoknaderSomIkkeErSendtTilNav() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(sendtNav = null)

        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify<InnsendingDAO>(innsendingDAO, never()).finnInnsendingForSykepengesoknad(any())
    }

    @Test
    fun behandlerIkkeSoknaderSomEttersendesTilArbeidsgiver() {
        val now = LocalDateTime.now()
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(sendtNav = now, sendtArbeidsgiver = now.plusHours(1))

        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify<InnsendingDAO>(innsendingDAO, never()).finnInnsendingForSykepengesoknad(any())
    }

    @Test
    fun behandlerSoknaderSomEttersendesTilNav() {
        val now = LocalDateTime.now()
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(sendtNav = now.plusNanos(1), sendtArbeidsgiver = now)

        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify<InnsendingDAO>(innsendingDAO).settBehandlet(any())
    }

    @Test
    fun behandlerSoknaderSomSkalTilNavOgArbeidsgiver() {
        val now = LocalDateTime.now()
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(sendtNav = now, sendtArbeidsgiver = now)

        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify<InnsendingDAO>(innsendingDAO).settBehandlet(any())
    }

    @Test
    fun behandlerSoknaderSomSkalTilNav() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)

        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify<InnsendingDAO>(innsendingDAO).settBehandlet(any())
    }

    @Test
    fun feilendeInnsendingLeggesPaRetryTopic() {
        given(behandleJournalConsumer.opprettJournalpost(any(), any()))
            .willThrow(RuntimeException("Opprett journal feilet"))

        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(rebehandlingProducer, times(1)).leggPaRebehandlingTopic(any(), any())
    }

    @Test
    fun soknadSomBehandlesLeggesIkkePaRetryTopic() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)

        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(rebehandlingProducer, never()).leggPaRebehandlingTopic(any(), any())
    }

    @Test
    fun brukerEksisterendeSakOmSoknadErPafolgende() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(fom = LocalDate.of(2019, 3, 11), tom = LocalDate.of(2019, 3, 20))

        given(innsendingDAO.finnTidligereInnsendinger("aktorId-745463060")).willReturn(listOf(TidligereInnsending("aktorId-745463060", "sak1", LocalDate.now(), LocalDate.of(2019, 3, 1),LocalDate.of(2019, 3, 10))))
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer, never()).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "sak1")
        verify(innsendingDAO).settBehandlet("innsending-guid")
    }

    @Test
    fun brukerIkkeEksisterendeSakOmSoknadIkkeErPafolgende() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(fom = LocalDate.of(2019, 3, 11), tom = LocalDate.of(2019, 3, 20))

        given(innsendingDAO.finnTidligereInnsendinger("aktorId-745463060")).willReturn(listOf(TidligereInnsending("aktorId-745463060", "sak1", LocalDate.now(), LocalDate.of(2019, 3, 1),LocalDate.of(2019, 3, 6))))
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "ny-sak-fra-gsak")
        verify(innsendingDAO).settBehandlet("innsending-guid")
    }

    @Test
    fun brukerEksisterendeSakOmSoknadErPafolgendeMedHelg() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(fom = LocalDate.of(2019, 3, 11), tom = LocalDate.of(2019, 3, 20))

        given(innsendingDAO.finnTidligereInnsendinger("aktorId-745463060")).willReturn(listOf(TidligereInnsending("aktorId-745463060", "sak1", LocalDate.now(), LocalDate.of(2019, 3, 1),LocalDate.of(2019, 3, 8))))
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer, never()).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "sak1")
        verify(innsendingDAO).settBehandlet("innsending-guid")
    }

    @Test
    fun brukerIkkeEksisterendeSakOmSoknadInnenforToDagerMenIkkeHelg() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(fom = LocalDate.of(2019, 3, 12), tom = LocalDate.of(2019, 3, 21))

        given(innsendingDAO.finnTidligereInnsendinger("aktorId-745463060")).willReturn(listOf(TidligereInnsending("aktorId-745463060", "sak1", LocalDate.now(), LocalDate.of(2019, 3, 1),LocalDate.of(2019, 3, 10))))
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "ny-sak-fra-gsak")
        verify(innsendingDAO).settBehandlet("innsending-guid")
    }

    @Test
    fun brukerIkkeEksisterendeSakOmViIkkeHarTidligereInnsending() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(fom = LocalDate.of(2019, 3, 12), tom = LocalDate.of(2019, 3, 21))

        given(innsendingDAO.finnTidligereInnsendinger("aktorId-745463060")).willReturn(emptyList())
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "ny-sak-fra-gsak")
        verify(innsendingDAO).settBehandlet("innsending-guid")
    }

    @Test
    fun brukerIkkeEksistrendeSakOmInnsendingErEtterSoknad() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(fom = LocalDate.of(2019, 2, 11), tom = LocalDate.of(2019, 2, 21))

        given(innsendingDAO.finnTidligereInnsendinger("aktorId-745463060")).willReturn(listOf(TidligereInnsending("aktorId-745463060", "sak1", LocalDate.now(), LocalDate.of(2019, 3, 1),LocalDate.of(2019, 3, 10))))
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "ny-sak-fra-gsak")
        verify(innsendingDAO).settBehandlet("innsending-guid")
    }

    @Test
    fun brukerEksisterendeSakOmSoknadErPafolgendeMedHelgFlereInnsendinger() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(fom = LocalDate.of(2019, 3, 11), tom = LocalDate.of(2019, 3, 20))

        given(innsendingDAO.finnTidligereInnsendinger("aktorId-745463060")).willReturn(listOf(
                TidligereInnsending("aktorId-745463060", "sak1", LocalDate.now(), LocalDate.of(2019, 2, 1),LocalDate.of(2019, 2, 8)),
                TidligereInnsending("aktorId-745463060", "sak2", LocalDate.now(), LocalDate.of(2019, 3, 1),LocalDate.of(2019, 3, 8)),
                TidligereInnsending("aktorId-745463060", "sak3", LocalDate.now(), LocalDate.of(2018, 3, 1),LocalDate.of(2018, 3, 8))))
        saksbehandlingsService.behandleSoknad(sykepengesoknad)

        verify(sakConsumer, never()).opprettSak(ArgumentMatchers.anyString())
        verify(innsendingDAO).oppdaterSaksId("innsending-guid", "sak2")
        verify(innsendingDAO).settBehandlet("innsending-guid")
    }
}

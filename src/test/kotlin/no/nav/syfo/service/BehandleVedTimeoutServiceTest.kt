package no.nav.syfo.service

import com.nhaarman.mockitokotlin2.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import no.nav.syfo.client.SyfosoknadClient
import no.nav.syfo.client.SøknadIkkeFunnetException
import no.nav.syfo.config.Toggle
import no.nav.syfo.innsending.InnsendingDbRecord
import no.nav.syfo.kafka.felles.*
import no.nav.syfo.repository.OppgaveStatus
import no.nav.syfo.repository.OppgavestyringDAO
import no.nav.syfo.repository.SpreOppgave
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class BehandleVedTimeoutServiceTest {
    @Mock
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @Mock
    lateinit var oppgavestyringDAO: OppgavestyringDAO

    @Mock
    lateinit var syfosoknadConsumer: SyfosoknadClient

    @Mock
    lateinit var toggle: Toggle

    @Mock
    lateinit var registry: MeterRegistry

    @Mock
    lateinit var identService: IdentService

    @InjectMocks
    lateinit var behandleVedTimeoutService: BehandleVedTimeoutService

    fun mockRegistry() {
        whenever(registry.counter(any(), any<Iterable<Tag>>())).thenReturn(mock())
    }

    fun mockHenting() {
        whenever(syfosoknadConsumer.hentSoknad(any())).thenReturn(
            SykepengesoknadDTO(
                id = UUID.randomUUID().toString(),
                opprettet = LocalDateTime.now(),
                fom = LocalDate.of(2019, 5, 4),
                tom = LocalDate.of(2019, 5, 8),
                type = SoknadstypeDTO.ARBEIDSTAKERE,
                sporsmal = listOf(
                    SporsmalDTO(
                        id = UUID.randomUUID().toString(),
                        tag = "TAGGEN",
                        sporsmalstekst = "Fungerer rebehandlinga?",
                        svartype = SvartypeDTO.JA_NEI,
                        svar = listOf(SvarDTO(verdi = "JA"))

                    )
                ),
                status = SoknadsstatusDTO.SENDT,
                sendtNav = LocalDateTime.now(),
                fnr = "fnr"
            )
        )
        whenever(identService.hentAktorIdForFnr(any())).thenReturn(
            "aktor"
        )
    }

    @Test
    fun `har ingenting å behandle`() {
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
    }

    @Test
    fun `har noe å behandle men mangler innsending`() {
        mockRegistry()
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    sykepengesoknadId = UUID.randomUUID().toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = false
                )
            )
        )
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
    }

    @Test
    fun `sletter ikke oppgave(i test) om vi mangler innsending og den er fersk`() {
        mockRegistry()
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    sykepengesoknadId = UUID.randomUUID().toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = false
                )
            )
        )
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
        verify(oppgavestyringDAO, never()).slettSpreOppgave(any())
    }

    @Test
    fun `sletter oppgave(i test) om vi mangler innsending og den er gammel`() {
        mockRegistry()
        whenever(toggle.isQ()).thenReturn(true)
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    sykepengesoknadId = UUID.randomUUID().toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusDays(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = false
                )
            )
        )
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
        verify(oppgavestyringDAO, times(1)).slettSpreOppgave(any())
    }

    @Test
    fun `har noe å behandle og har innsending`() {
        mockHenting()
        mockRegistry()
        val søknadsId = UUID.randomUUID().toString()
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    sykepengesoknadId = søknadsId,
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(søknadsId)).thenReturn(
            InnsendingDbRecord(
                id = "iid",
                sykepengesoknadId = søknadsId,
                journalpostId = "journalpost"
            )
        )
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, times(1)).opprettOppgave(any(), any(), any())
        verify(oppgavestyringDAO, times(1)).oppdaterOppgave(UUID.fromString(søknadsId), null, OppgaveStatus.Opprettet)
    }

    @Test
    fun `flere oppgaver hvor en tryner`() {
        mockRegistry()
        mockHenting()
        val søknadsId1 = UUID.randomUUID()
        val søknadsId2 = UUID.randomUUID()
        val søknadsId3 = UUID.randomUUID()
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    sykepengesoknadId = søknadsId1.toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                ),
                SpreOppgave(
                    sykepengesoknadId = søknadsId2.toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                ),
                SpreOppgave(
                    sykepengesoknadId = søknadsId3.toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(any())).thenAnswer {
            InnsendingDbRecord(
                id = "iid",
                sykepengesoknadId = it.arguments[0].toString(),
                journalpostId = "journalpost"
            )
        }
        whenever(syfosoknadConsumer.hentSoknad(søknadsId2.toString())).thenThrow(RuntimeException("I AM ERROR"))
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, times(2)).opprettOppgave(any(), any(), any())
        verify(oppgavestyringDAO, times(1)).oppdaterOppgave(søknadsId1, null, OppgaveStatus.Opprettet)
        verify(oppgavestyringDAO, times(1)).oppdaterOppgave(søknadsId3, null, OppgaveStatus.Opprettet)
    }

    @Test
    fun `Finner ikke søknad, skippes i Q`() {
        val søknadsId1 = UUID.randomUUID()
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    sykepengesoknadId = søknadsId1.toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(søknadsId1.toString())).thenReturn(
            InnsendingDbRecord(
                id = "iid",
                sykepengesoknadId = søknadsId1.toString(),
                journalpostId = "journalpost"
            )
        )
        whenever(toggle.isQ()).thenReturn(true)
        whenever(syfosoknadConsumer.hentSoknad(søknadsId1.toString())).thenThrow(SøknadIkkeFunnetException("finner ikke"))
        behandleVedTimeoutService.behandleTimeout()

        verify(oppgavestyringDAO, times(1)).oppdaterOppgave(søknadsId1, null, OppgaveStatus.IkkeOpprett)
    }

    @Test
    fun `Finner ikke søknad, skippes ikke i P`() {
        val søknadsId1 = UUID.randomUUID()
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    sykepengesoknadId = søknadsId1.toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(søknadsId1.toString())).thenReturn(
            InnsendingDbRecord(
                id = "iid",
                sykepengesoknadId = søknadsId1.toString(),
                journalpostId = "journalpost"
            )
        )
        whenever(toggle.isQ()).thenReturn(false)
        whenever(syfosoknadConsumer.hentSoknad(søknadsId1.toString())).thenThrow(SøknadIkkeFunnetException("msg"))
        assertThrows(SøknadIkkeFunnetException::class.java) {
            behandleVedTimeoutService.behandleTimeout()
        }
        verify(oppgavestyringDAO, never()).oppdaterOppgave(any(), any(), any())
    }
}

package no.nav.syfo.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.syfo.config.Toggle
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.repository.SpreOppgave
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import no.nav.syfo.consumer.syfosoknad.SøknadIkkeFunnetException
import no.nav.syfo.domain.Innsending
import no.nav.syfo.kafka.felles.SkjultVerdi
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SporsmalDTO
import no.nav.syfo.kafka.felles.SvarDTO
import no.nav.syfo.kafka.felles.SvartypeDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class BehandleVedTimeoutServiceTest {
    @Mock
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @Mock
    lateinit var oppgavestyringDAO: OppgavestyringDAO

    @Mock
    lateinit var syfosoknadConsumer: SyfosoknadConsumer

    @Mock
    lateinit var toggle: Toggle

    @InjectMocks
    lateinit var behandleVedTimeoutService: BehandleVedTimeoutService

    fun mockHenting() {
        whenever(syfosoknadConsumer.hentSoknad(any())).thenReturn(
            SykepengesoknadDTO(
                aktorId = "aktor",
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
                fodselsnummer = SkjultVerdi("fnr")
            )
        )
    }

    @Test
    fun `har ingenting å behandle`() {
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any())
    }

    @Test
    fun `har noe å behandle men mangler innsending`() {
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    søknadsId = UUID.randomUUID().toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = false
                )
            )
        )
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any())
    }

    @Test
    fun `sletter ikke oppgave(i test) om vi mangler innsending og den er fersk`() {
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    søknadsId = UUID.randomUUID().toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = false
                )
            )
        )
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any())
        verify(oppgavestyringDAO, never()).slettSpreOppgave(any())
    }

    @Test
    fun `sletter oppgave(i test) om vi mangler innsending og den er gammel`() {

        whenever(toggle.isQ()).thenReturn(true)
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    søknadsId = UUID.randomUUID().toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusDays(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = false
                )
            )
        )
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any())
        verify(oppgavestyringDAO, times(1)).slettSpreOppgave(any())
    }

    @Test
    fun `har noe å behandle og har innsending`() {
        mockHenting()
        val søknadsId = UUID.randomUUID().toString()
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    søknadsId = søknadsId,
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(søknadsId)).thenReturn(
            Innsending(
                innsendingsId = "iid",
                ressursId = søknadsId,
                aktorId = "aktor",
                saksId = "saksId",
                journalpostId = "journalpost"
            )
        )
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, times(1)).opprettOppgave(any(), any())
        verify(oppgavestyringDAO, times(1)).oppdaterOppgave(UUID.fromString(søknadsId), null, OppgaveStatus.Opprettet)
    }

    @Test
    fun `flere oppgaver hvor en tryner`() {
        mockHenting()
        val søknadsId1 = UUID.randomUUID()
        val søknadsId2 = UUID.randomUUID()
        val søknadsId3 = UUID.randomUUID()
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    søknadsId = søknadsId1.toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                ),
                SpreOppgave(
                    søknadsId = søknadsId2.toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                ),
                SpreOppgave(
                    søknadsId = søknadsId3.toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(any())).thenAnswer {
            Innsending(
                innsendingsId = "iid",
                ressursId = it.arguments[0].toString(),
                aktorId = "aktor",
                saksId = "saksId",
                journalpostId = "journalpost"
            )
        }
        whenever(syfosoknadConsumer.hentSoknad(søknadsId2.toString())).thenThrow(RuntimeException("I AM ERROR"))
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, times(2)).opprettOppgave(any(), any())
        verify(oppgavestyringDAO, times(1)).oppdaterOppgave(søknadsId1, null, OppgaveStatus.Opprettet)
        verify(oppgavestyringDAO, times(1)).oppdaterOppgave(søknadsId3, null, OppgaveStatus.Opprettet)
    }

    @Test
    fun `Finner ikke søknad, skippes i Q`() {
        val søknadsId1 = UUID.randomUUID()
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    søknadsId = søknadsId1.toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(søknadsId1.toString())).thenReturn(
            Innsending(
                innsendingsId = "iid",
                ressursId = søknadsId1.toString(),
                aktorId = "aktor",
                saksId = "saksId",
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
                    søknadsId = søknadsId1.toString(),
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(søknadsId1.toString())).thenReturn(
            Innsending(
                innsendingsId = "iid",
                ressursId = søknadsId1.toString(),
                aktorId = "aktor",
                saksId = "saksId",
                journalpostId = "journalpost"
            )
        )
        whenever(toggle.isQ()).thenReturn(false)
        whenever(syfosoknadConsumer.hentSoknad(søknadsId1.toString())).thenThrow(SøknadIkkeFunnetException("msg"))
        behandleVedTimeoutService.behandleTimeout()
        verify(oppgavestyringDAO, never()).oppdaterOppgave(any(), any(), any())
    }
}

package no.nav.syfo.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.repository.SpreOppgave
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import no.nav.syfo.domain.Innsending
import no.nav.syfo.kafka.felles.SkjultVerdi
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SporsmalDTO
import no.nav.syfo.kafka.felles.SvarDTO
import no.nav.syfo.kafka.felles.SvartypeDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class BehandleVedTimeoutServiceTest {
    @Mock
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @Mock
    lateinit var oppgavestyringDAO: OppgavestyringDAO

    @Mock
    lateinit var syfosoknadConsumer: SyfosoknadConsumer

    @InjectMocks
    lateinit var behandleVedTimeoutService: BehandleVedTimeoutService

    @Before
    fun setup() {
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
                    søknadsId = "sid",
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1)
                )
            )
        )
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any())
    }

    @Test
    fun `har noe å behandle og har innsending`() {
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    søknadsId = "sid",
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1)
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending("sid")).thenReturn(
            Innsending(
                innsendingsId = "iid",
                ressursId = "sid",
                aktorId = "aktor",
                saksId = "saksId",
                journalpostId = "journalpost"
            )
        )
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, times(1)).opprettOppgave(any(), any())
        verify(oppgavestyringDAO, times(1)).settStatus("sid", OppgaveStatus.Opprettet)
    }

    @Test
    fun `flere oppgaver hvor en tryner`() {
        whenever(oppgavestyringDAO.hentOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgave(
                    søknadsId = "sid",
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1)
                ),
                SpreOppgave(
                    søknadsId = "sid1",
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1)
                ),
                SpreOppgave(
                    søknadsId = "sid2",
                    timeout = LocalDateTime.now().minusHours(1),
                    status = OppgaveStatus.Utsett,
                    opprettet = LocalDateTime.now().minusHours(2),
                    modifisert = LocalDateTime.now().minusHours(1)
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(any())).thenReturn(
            Innsending(
                innsendingsId = "iid",
                ressursId = "sid",
                aktorId = "aktor",
                saksId = "saksId",
                journalpostId = "journalpost"
            )
        )
        whenever(syfosoknadConsumer.hentSoknad("sid1")).thenThrow(RuntimeException("I AM ERROR"))
        behandleVedTimeoutService.behandleTimeout()
        verify(saksbehandlingsService, times(2)).opprettOppgave(any(), any())
        verify(oppgavestyringDAO, times(1)).settStatus("sid", OppgaveStatus.Opprettet)
        verify(oppgavestyringDAO, times(1)).settStatus("sid2", OppgaveStatus.Opprettet)
    }
}

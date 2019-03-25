package no.nav.syfo.service

import no.nav.syfo.any
import no.nav.syfo.consumer.aktor.AktorConsumer
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.domain.Innsending
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Soknadstype.SELVSTENDIGE_OG_FRILANSERE
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.eq
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate

@RunWith(MockitoJUnitRunner::class)
class BehandleFeiledeSoknaderServiceTest {

    @Mock
    private lateinit var aktorConsumer: AktorConsumer

    @Mock
    private lateinit var innsendingDAO: InnsendingDAO

    @Mock
    private lateinit var saksbehandlingsService: SaksbehandlingsService

    @InjectMocks
    private lateinit var behandleFeiledeSoknaderService: BehandleFeiledeSoknaderService

    private lateinit var sykepengesoknad: Sykepengesoknad
    private lateinit var globalInnsending: Innsending

    @Before
    fun setup() {
        sykepengesoknad = Sykepengesoknad(
                id = "soknadsId",
                aktorId = "aktorId",
                soknadstype = SELVSTENDIGE_OG_FRILANSERE,
                sporsmal = emptyList(),
                sykmeldingId = "sykmeldingsId",
                fom = LocalDate.of(2018, 9, 7),
                tom = LocalDate.of(2018, 9, 13),
                status = "SENDT",
                sendtNav = LocalDate.of(2018, 9, 13).atStartOfDay(),
                opprettet = LocalDate.of(2018, 9, 7).atStartOfDay()

        )

        globalInnsending = Innsending("innsendingsId", "soknadsId")

        given(saksbehandlingsService.finnEllerOpprettSak("innsendingsId", "aktorId", sykepengesoknad.fom)).willReturn("saksId")
        given(saksbehandlingsService.opprettJournalpost(anyString(), no.nav.syfo.any(), anyString())).willReturn("journalpostId")
        given(aktorConsumer.finnFnr("aktorId")).willReturn("fnr")
        given(saksbehandlingsService.opprettSoknad(no.nav.syfo.any(), anyString())).willReturn(Soknad.lagSoknad(sykepengesoknad, "fnr", "Ola Nordmann"))
    }

    @Test
    fun rebehandlerInnsendingSomHarFeiletIAktor() {
        behandleFeiledeSoknaderService.behandleFeiletSoknad(globalInnsending, sykepengesoknad)

        verify<InnsendingDAO>(innsendingDAO).oppdaterAktorId("innsendingsId", "aktorId")

        verify<SaksbehandlingsService>(saksbehandlingsService).finnEllerOpprettSak(anyString(), anyString(), any())
        verify<SaksbehandlingsService>(saksbehandlingsService).opprettJournalpost(anyString(), no.nav.syfo.any(), anyString())
        verify<SaksbehandlingsService>(saksbehandlingsService).opprettOppgave(eq("innsendingsId"), anyString(), anyString(), no.nav.syfo.any(), anyString(), anyString())

        verify<InnsendingDAO>(innsendingDAO).settBehandlet("innsendingsId")
        verify<InnsendingDAO>(innsendingDAO).fjernFeiletInnsending("innsendingsId")
    }

    @Test
    fun rebehandlerInnsendingSomHarFeiletISak() {
        val innsending = globalInnsending.copy(globalInnsending.innsendingsId, globalInnsending.ressursId, "aktorId")

        behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad)

        verify<InnsendingDAO>(innsendingDAO, never()).oppdaterAktorId("innsendingsId", "aktorId")

        verify<SaksbehandlingsService>(saksbehandlingsService).finnEllerOpprettSak(anyString(), anyString(), any())
        verify<SaksbehandlingsService>(saksbehandlingsService).opprettJournalpost(anyString(), no.nav.syfo.any(), anyString())
        verify<SaksbehandlingsService>(saksbehandlingsService).opprettOppgave(anyString(), anyString(), anyString(), no.nav.syfo.any(), anyString(), anyString())

        verify<InnsendingDAO>(innsendingDAO).settBehandlet("innsendingsId")
        verify<InnsendingDAO>(innsendingDAO).fjernFeiletInnsending("innsendingsId")
    }

    @Test
    fun rebehandlerInnsendingSomHarFeiletIJournalPost() {
        val innsending = globalInnsending.copy(globalInnsending.innsendingsId, globalInnsending.ressursId, "aktorId", "saksId")

        behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad)

        verify<SaksbehandlingsService>(saksbehandlingsService, never()).finnEllerOpprettSak(anyString(), anyString(), any())
        verify<SaksbehandlingsService>(saksbehandlingsService).opprettJournalpost(anyString(), no.nav.syfo.any(), anyString())
        verify<SaksbehandlingsService>(saksbehandlingsService).opprettOppgave(anyString(), anyString(), anyString(), no.nav.syfo.any(), anyString(), anyString())

        verify<InnsendingDAO>(innsendingDAO).settBehandlet("innsendingsId")
        verify<InnsendingDAO>(innsendingDAO).fjernFeiletInnsending("innsendingsId")
    }

    @Test
    fun rebehandlerInnsendingSomHarFeiletIOppgave() {
        val innsending = globalInnsending.copy(globalInnsending.innsendingsId, globalInnsending.ressursId, "aktorId", "saksId", "journalpostId")

        behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad)

        verify<SaksbehandlingsService>(saksbehandlingsService, never()).finnEllerOpprettSak(anyString(), anyString(), any())
        verify<SaksbehandlingsService>(saksbehandlingsService, never()).opprettJournalpost(anyString(), no.nav.syfo.any(), anyString())
        verify<SaksbehandlingsService>(saksbehandlingsService).opprettOppgave(anyString(), anyString(), anyString(), no.nav.syfo.any(), anyString(), anyString())

        verify<InnsendingDAO>(innsendingDAO).settBehandlet("innsendingsId")
        verify<InnsendingDAO>(innsendingDAO).fjernFeiletInnsending("innsendingsId")
    }
}

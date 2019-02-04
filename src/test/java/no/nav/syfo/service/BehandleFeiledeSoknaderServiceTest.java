package no.nav.syfo.service;

import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.consumer.ws.AktorConsumer;
import no.nav.syfo.domain.Innsending;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;

import static no.nav.syfo.domain.dto.Soknadstype.SELVSTENDIGE_OG_FRILANSERE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BehandleFeiledeSoknaderServiceTest {

    @Mock
    private AktorConsumer aktorConsumer;

    @Mock
    private InnsendingDAO innsendingDAO;

    @Mock
    private SaksbehandlingsService saksbehandlingsService;

    @InjectMocks
    private BehandleFeiledeSoknaderService behandleFeiledeSoknaderService;

    private Sykepengesoknad sykepengesoknad;
    private Innsending globalInnsending;

    @Before
    public void setup() {
        sykepengesoknad = Sykepengesoknad.builder()
                .id("soknadsId")
                .aktorId("aktorId")
                .soknadstype(SELVSTENDIGE_OG_FRILANSERE)
                .sporsmal(Collections.emptyList())
                .sykmeldingId("sykmeldingsId")
                .fom(LocalDate.of(2018, 9, 7))
                .tom(LocalDate.of(2018, 9, 13))
                .status("SENDT")
                .sendtNav(LocalDate.of(2018, 9, 13).atStartOfDay())
                .opprettet(LocalDate.of(2018, 9, 7).atStartOfDay())
                .build();

        globalInnsending = Innsending.builder()
                .innsendingsId("innsendingsId")
                .ressursId("soknadsId")
                .build();

        when(saksbehandlingsService.opprettSak("innsendingsId", "fnr")).thenReturn("saksId");
        when(saksbehandlingsService.opprettJournalpost(anyString(), any(Soknad.class), anyString())).thenReturn("journalpostId");
        when(aktorConsumer.finnFnr("aktorId")).thenReturn("fnr");
        when(saksbehandlingsService.opprettSoknad(any(), anyString())).thenReturn(Soknad.lagSoknad(sykepengesoknad, "fnr", "Ola Nordmann"));
    }

    @Test
    public void rebehandlerInnsendingSomHarFeiletIAktor() {
        behandleFeiledeSoknaderService.behandleFeiletSoknad(globalInnsending, sykepengesoknad);

        verify(innsendingDAO).oppdaterAktorId("innsendingsId", "aktorId");

        verify(saksbehandlingsService).opprettSak(anyString(), anyString());
        verify(saksbehandlingsService).opprettJournalpost(anyString(), any(Soknad.class), anyString());
        verify(saksbehandlingsService).opprettOppgave(anyString(), anyString(), any(Soknad.class), anyString(), anyString());

        verify(innsendingDAO).settBehandlet("innsendingsId");
        verify(innsendingDAO).fjernFeiletInnsending("innsendingsId");
    }

    @Test
    public void rebehandlerInnsendingSomHarFeiletISak() {
        Innsending innsending = globalInnsending.toBuilder()
                .aktorId("aktorId")
                .build();

        behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad);

        verify(innsendingDAO, never()).oppdaterAktorId("innsendingsId", "aktorId");

        verify(saksbehandlingsService).opprettSak(anyString(), anyString());
        verify(saksbehandlingsService).opprettJournalpost(anyString(), any(Soknad.class), anyString());
        verify(saksbehandlingsService).opprettOppgave(anyString(), anyString(), any(Soknad.class), anyString(), anyString());

        verify(innsendingDAO).settBehandlet("innsendingsId");
        verify(innsendingDAO).fjernFeiletInnsending("innsendingsId");
    }

    @Test
    public void rebehandlerInnsendingSomHarFeiletIJournalPost() {
        Innsending innsending = globalInnsending.toBuilder()
                .aktorId("aktorId")
                .saksId("saksId")
                .build();

        behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad);

        verify(saksbehandlingsService, never()).opprettSak(anyString(), anyString());
        verify(saksbehandlingsService).opprettJournalpost(anyString(), any(Soknad.class), anyString());
        verify(saksbehandlingsService).opprettOppgave(anyString(), anyString(), any(Soknad.class), anyString(), anyString());

        verify(innsendingDAO).settBehandlet("innsendingsId");
        verify(innsendingDAO).fjernFeiletInnsending("innsendingsId");
    }

    @Test
    public void rebehandlerInnsendingSomHarFeiletIOppgave() {
        Innsending innsending = globalInnsending.toBuilder()
                .aktorId("aktorId")
                .saksId("saksId")
                .journalpostId("journalpostId")
                .build();

        behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad);

        verify(saksbehandlingsService, never()).opprettSak(anyString(), anyString());
        verify(saksbehandlingsService, never()).opprettJournalpost(anyString(), any(Soknad.class), anyString());
        verify(saksbehandlingsService).opprettOppgave(anyString(), anyString(), any(Soknad.class), anyString(), anyString());

        verify(innsendingDAO).settBehandlet("innsendingsId");
        verify(innsendingDAO).fjernFeiletInnsending("innsendingsId");
    }


}
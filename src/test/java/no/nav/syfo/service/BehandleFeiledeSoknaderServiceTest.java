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
    private Innsending innsending;

    @Before
    public void setup() {
        sykepengesoknad = new Sykepengesoknad();
        sykepengesoknad.setId("soknadsId");
        sykepengesoknad.setAktorId("aktorId");
        sykepengesoknad.setSoknadstype(SELVSTENDIGE_OG_FRILANSERE);
        sykepengesoknad.setSporsmal(Collections.emptyList());
        sykepengesoknad.setSykmeldingId("sykmeldingsId");
        sykepengesoknad.setFom(LocalDate.of(2018, 9, 7));
        sykepengesoknad.setTom(LocalDate.of(2018, 9, 13));
        sykepengesoknad.setStatus("SENDT");
        sykepengesoknad.setInnsendtDato(LocalDate.of(2018, 9, 13));
        sykepengesoknad.setOpprettetDato(LocalDate.of(2018, 9, 7));

        innsending = Innsending.builder()
                .innsendingsId("innsendingsId")
                .ressursId("soknadsId")
                .build();

        when(saksbehandlingsService.opprettSak("innsendingsId", "fnr")).thenReturn("saksId");
        when(saksbehandlingsService.opprettJournalpost(anyString(),any(Soknad.class),anyString())).thenReturn("journalpostId");
        when(aktorConsumer.finnFnr("aktorId")).thenReturn("fnr");
        when(saksbehandlingsService.opprettSoknad(any(), anyString())).thenReturn(Soknad.lagSoknad(sykepengesoknad, "fnr", "Ola Nordmann"));
    }

    @Test
    public void rebehandlerInnsendingSomHarFeiletIAktor() {
        behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad);

        verify(innsendingDAO).oppdaterAktorId("innsendingsId", "aktorId");

        verify(saksbehandlingsService).opprettSak(anyString(), anyString());
        verify(saksbehandlingsService).opprettJournalpost(anyString(), any(Soknad.class), anyString());
        verify(saksbehandlingsService).opprettOppgave(anyString(), anyString(), any(Soknad.class), anyString(), anyString());

        verify(innsendingDAO).settBehandlet("innsendingsId");
        verify(innsendingDAO).fjernFeiletInnsending("innsendingsId");
    }

    @Test
    public void rebehandlerInnsendingSomHarFeiletISak() {
        innsending.setAktorId("aktorId");

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
        innsending.setAktorId("aktorId");
        innsending.setSaksId("saksId");

        behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad);

        verify(innsendingDAO, never()).oppdaterAktorId("innsendingsId", "aktorId");

        verify(saksbehandlingsService, never()).opprettSak(anyString(), anyString());
        verify(saksbehandlingsService).opprettJournalpost(anyString(), any(Soknad.class), anyString());
        verify(saksbehandlingsService).opprettOppgave(anyString(), anyString(), any(Soknad.class), anyString(), anyString());

        verify(innsendingDAO).settBehandlet("innsendingsId");
        verify(innsendingDAO).fjernFeiletInnsending("innsendingsId");
    }

    @Test
    public void rebehandlerInnsendingSomHarFeiletIOppgave() {
        innsending.setAktorId("aktorId");
        innsending.setSaksId("saksId");
        innsending.setJournalpostId("journalpostId");
        behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad);

        verify(innsendingDAO, never()).oppdaterAktorId("innsendingsId", "aktorId");

        verify(saksbehandlingsService, never()).opprettSak(anyString(), anyString());
        verify(saksbehandlingsService, never()).opprettJournalpost(anyString(), any(Soknad.class), anyString());
        verify(saksbehandlingsService).opprettOppgave(anyString(), anyString(), any(Soknad.class), anyString(), anyString());

        verify(innsendingDAO).settBehandlet("innsendingsId");
        verify(innsendingDAO).fjernFeiletInnsending("innsendingsId");
    }


}
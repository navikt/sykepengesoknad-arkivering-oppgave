package no.nav.syfo.service;

import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.consumer.ws.AktorConsumer;
import no.nav.syfo.consumer.ws.BehandleJournalConsumer;
import no.nav.syfo.consumer.ws.BehandleSakConsumer;
import no.nav.syfo.consumer.ws.PersonConsumer;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SaksbehandlingsServiceTest {

    @Mock
    private AktorConsumer aktorConsumer;
    @Mock
    private PersonConsumer personConsumer;
    @Mock
    private InnsendingDAO innsendingDAO;
    @Mock
    private BehandleSakConsumer behandleSakConsumer;
    @Mock
    private BehandleJournalConsumer behandleJournalConsumer;

    @InjectMocks
    SaksbehandlingsService saksbehandlingsService;

    @Test
    public void handtereBehandleSoknad() {
        when(aktorConsumer.finnFnr(any())).thenReturn("12345678901");
        when(personConsumer.finnBrukerPersonnavnByFnr(any())).thenReturn("Personnavn");
        when(innsendingDAO.opprettInnsending()).thenReturn("uuid");
        when(behandleSakConsumer.opprettSak(any())).thenReturn("saksId");
        when(behandleJournalConsumer.opprettJournalpost(any(), any())).thenThrow(new RuntimeException("Opprett journal feilet"));

        Sykepengesoknad sykepengesoknad = new Sykepengesoknad();
        sykepengesoknad.setId("id");
        sykepengesoknad.setSykmeldingId("sykmeldingId");
        sykepengesoknad.setAktorId("aktorId");
        sykepengesoknad.setSoknadstype(Soknadstype.SELVSTENDIGE_OG_FRILANSERE);
        sykepengesoknad.setStatus("status");
        sykepengesoknad.setFom(LocalDate.now());
        sykepengesoknad.setTom(LocalDate.now());
        sykepengesoknad.setOpprettetDato(LocalDate.now());
        sykepengesoknad.setInnsendtDato(LocalDate.now());

        saksbehandlingsService.behandleSoknad(sykepengesoknad);

        verify(innsendingDAO, times(1)).leggTilFeiletInnsending(any());
    }
}
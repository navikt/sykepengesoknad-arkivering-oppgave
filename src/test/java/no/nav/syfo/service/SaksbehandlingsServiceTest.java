package no.nav.syfo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.consumer.ws.*;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static no.nav.syfo.TestUtils.soknadSelvstendigMedNeisvar;
import static no.nav.syfo.domain.dto.Soknadstype.SELVSTENDIGE_OG_FRILANSERE;
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
    @Mock
    private BehandlendeEnhetService behandlendeEnhetService;
    @Mock
    private OppgavebehandlingConsumer oppgavebehandlingConsumer;
    @Mock
    private MeterRegistry registry;

    @InjectMocks
    private SaksbehandlingsService saksbehandlingsService;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Before
    public void setup() {
        when(aktorConsumer.finnFnr(any())).thenReturn("12345678901");
        when(personConsumer.finnBrukerPersonnavnByFnr(any())).thenReturn("Personnavn");
        when(behandleSakConsumer.opprettSak(any())).thenReturn("saksId");
        when(behandleJournalConsumer.opprettJournalpost(any(), any())).thenReturn("journalpostId");
        when(behandlendeEnhetService.hentBehandlendeEnhet("12345678901", SELVSTENDIGE_OG_FRILANSERE)).thenReturn("2017");
        when(oppgavebehandlingConsumer.opprettOppgave(anyString(), anyString(), anyString(), anyString(), any(Soknad.class))).thenReturn("oppgaveId");
        when(registry.counter(any(), anyIterable())).thenReturn(mock(Counter.class));
    }

    @Test
    public void behandlerInnsending() throws IOException {
        Sykepengesoknad sykepengesoknad = objectMapper.readValue(soknadSelvstendigMedNeisvar, Sykepengesoknad.class);

        saksbehandlingsService.behandleSoknad(sykepengesoknad);

        verify(innsendingDAO).settBehandlet(any());
    }

    @Test
    public void feilendeInnsendingLeggesIBasen() throws IOException {
        when(behandleJournalConsumer.opprettJournalpost(any(), any()))
                .thenThrow(new RuntimeException("Opprett journal feilet"));

        Sykepengesoknad sykepengesoknad = objectMapper.readValue(soknadSelvstendigMedNeisvar, Sykepengesoknad.class);
        saksbehandlingsService.behandleSoknad(sykepengesoknad);

        verify(innsendingDAO, times(1)).leggTilFeiletInnsending(any());
    }
}
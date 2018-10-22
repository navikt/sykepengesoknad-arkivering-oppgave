package no.nav.syfo.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
    @Mock
    private MeterRegistry registry;

    @InjectMocks
    private SaksbehandlingsService saksbehandlingsService;

    @Test
    public void handtereBehandleSoknad() {
        when(aktorConsumer.finnFnr(any())).thenReturn("12345678901");
        when(personConsumer.finnBrukerPersonnavnByFnr(any())).thenReturn("Personnavn");
        when(innsendingDAO.opprettInnsending("ressursId", "aktorId")).thenReturn("uuid");
        when(behandleSakConsumer.opprettSak(any())).thenReturn("saksId");
        when(behandleJournalConsumer.opprettJournalpost(any(), any()))
                .thenThrow(new RuntimeException("Opprett journal feilet"));
        when(registry.counter(any(), anyIterable())).thenReturn(mock(Counter.class));

        Sykepengesoknad sykepengesoknad = Sykepengesoknad.builder()
                .id("ressursId")
                .sykmeldingId("sykmeldingId")
                .aktorId("aktorId")
                .soknadstype(Soknadstype.SELVSTENDIGE_OG_FRILANSERE)
                .status("status")
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .opprettetDato(LocalDate.now())
                .innsendtDato(LocalDate.now())
                .build();

        saksbehandlingsService.behandleSoknad(sykepengesoknad);

        verify(innsendingDAO, times(1)).leggTilFeiletInnsending(any());
    }
}
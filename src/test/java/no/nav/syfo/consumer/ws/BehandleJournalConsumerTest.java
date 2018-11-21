package no.nav.syfo.consumer.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.controller.PDFRestController;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.WSJournalfoerInngaaendeHenvendelseResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static no.nav.syfo.TestUtils.soknadSelvstendigMedNeisvar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class BehandleJournalConsumerTest {

    @Mock
    private BehandleJournalV2 behandleJournalV2;
    @Mock
    private PersonConsumer personConsumer;
    @Mock
    private PDFRestController pdfRestController;

    @InjectMocks
    private BehandleJournalConsumer behandleJournalConsumer;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    public void opprettJournalpost() throws IOException {
        when(behandleJournalV2.journalfoerInngaaendeHenvendelse(any()))
                .thenReturn(new WSJournalfoerInngaaendeHenvendelseResponse().withJournalpostId("id"));

        Sykepengesoknad sykepengesoknad = objectMapper.readValue(soknadSelvstendigMedNeisvar, Sykepengesoknad.class);
        Soknad soknad = Soknad.lagSoknad(sykepengesoknad, "22026900623", "Kjersti Glad");
        String id = behandleJournalConsumer.opprettJournalpost(soknad, "saksId");

        assertThat(id).isEqualTo("id");
    }

    @Test
    public void opprettJournalpostTaklerFeil() throws IOException {
        when(behandleJournalV2.journalfoerInngaaendeHenvendelse(any())).thenThrow(new RuntimeException("test"));

        Sykepengesoknad sykepengesoknad = objectMapper.readValue(soknadSelvstendigMedNeisvar, Sykepengesoknad.class);
        Soknad soknad = Soknad.lagSoknad(sykepengesoknad, "22026900623", "Kjersti Glad");

        try {
            behandleJournalConsumer.opprettJournalpost(soknad, "saksid");
        } catch (RuntimeException e) {
            assertThat(e).hasMessage("Kunne ikke behandle journalpost for søknad med id daa8b4b5-ece8-4e6d-ab7e-c7354958201a og saks id: saksid");
        }
    }
}
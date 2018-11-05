package no.nav.syfo.consumer.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.controller.PDFRestController;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.WSJournalfoerInngaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.WSJournalfoerInngaaendeHenvendelseResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

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

    @Test
    public void opprettJournalpost() throws IOException {
        ArgumentCaptor<WSJournalfoerInngaaendeHenvendelseRequest> captor = ArgumentCaptor.forClass(WSJournalfoerInngaaendeHenvendelseRequest.class);
        when(behandleJournalV2.journalfoerInngaaendeHenvendelse(captor.capture()))
                .thenReturn(new WSJournalfoerInngaaendeHenvendelseResponse().withJournalpostId("id"));

        String serialisertSoknad = "{\"id\":\"test-kafka-sykepengesoknad\",\"aktorId\":\"aktorId\",\"sykmeldingId\":\"sykmelding-id\",\"soknadstype\":\"SELVSTENDIGE_OG_FRILANSERE\",\"status\":\"TIL_SENDING\",\"fom\":\"2018-06-06\",\"tom\":\"2018-07-07\",\"opprettetDato\":\"2018-06-06\",\"sporsmal\":[{\"id\":\"1\",\"tag\":null,\"sporsmalstekst\":\"Dette er et testspørsmål\",\"undertekst\":null,\"svartype\":\"PROSENT\",\"min\":null,\"max\":null,\"kriterieForVisningAvUndersporsmal\":null,\"svar\":[{\"verdi\":\"69\"}],\"undersporsmal\":null}],\"innsendtDato\":\"2018-06-20\"}";
        Sykepengesoknad sykepengesoknad = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(serialisertSoknad, Sykepengesoknad.class);
        Soknad soknad = Soknad.lagSoknad(sykepengesoknad, "00000011111", "Kjersti Glad");
        String id = behandleJournalConsumer.opprettJournalpost(soknad, "saksId");

        WSJournalfoerInngaaendeHenvendelseRequest request = captor.getValue();

        assertThat(request.getJournalpost().getJournalfoerendeEnhetREF()).isEqualTo("9999");
        assertThat(request.getJournalpost().getOpprettetAvNavn()).isEqualTo("Automatisk jobb");
        assertThat(request.getJournalpost().getDokumentinfoRelasjon().get(0).getJournalfoertDokument().getDokumentType().getValue()).isEqualTo("søknadsyk");
        assertThat(request.getJournalpost().getDokumentinfoRelasjon().get(0).getJournalfoertDokument().getKategorikode()).isEqualTo("søknadsyk");
        assertThat(request.getJournalpost().getDokumentinfoRelasjon().get(0).getJournalfoertDokument().getBeskriverInnhold().get(0).getFiltype().getValue()).isEqualTo("PDFA");


        assertThat(id).isEqualTo("id");
    }

    @Test
    public void opprettJournalpostTaklerFeil() throws IOException {
        when(behandleJournalV2.journalfoerInngaaendeHenvendelse(any())).thenThrow(new RuntimeException("test"));

        String serialisertSoknad = "{\"id\":\"test-kafka-sykepengesoknad\",\"aktorId\":\"aktorId\",\"sykmeldingId\":\"sykmelding-id\",\"soknadstype\":\"SELVSTENDIGE_OG_FRILANSERE\",\"status\":\"TIL_SENDING\",\"fom\":\"2018-06-06\",\"tom\":\"2018-07-07\",\"opprettetDato\":\"2018-06-06\",\"sporsmal\":[{\"id\":\"1\",\"tag\":null,\"sporsmalstekst\":\"Dette er et testspørsmål\",\"undertekst\":null,\"svartype\":\"PROSENT\",\"min\":null,\"max\":null,\"kriterieForVisningAvUndersporsmal\":null,\"svar\":[{\"verdi\":\"69\"}],\"undersporsmal\":null}],\"innsendtDato\":\"2018-06-20\"}";
        Sykepengesoknad sykepengesoknad = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(serialisertSoknad, Sykepengesoknad.class);
        Soknad soknad = Soknad.lagSoknad(sykepengesoknad, "00000011111", "Kjersti Glad");

        try {
            behandleJournalConsumer.opprettJournalpost(soknad, "saksid");
        } catch (RuntimeException e) {
            assertThat(e).hasMessage("Kunne ikke behandle journalpost for søknad med id test-kafka-sykepengesoknad og saks id: saksid");
        }
    }
}
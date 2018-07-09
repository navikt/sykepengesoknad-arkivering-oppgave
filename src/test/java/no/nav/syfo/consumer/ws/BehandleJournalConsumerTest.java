package no.nav.syfo.consumer.ws;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.WSJournalfoerInngaaendeHenvendelseResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BehandleJournalConsumerTest {

    @Mock
    private BehandleJournalV2 behandleJournalV2;

    @Mock
    private PersonConsumer personConsumer;

    @InjectMocks
    private BehandleJournalConsumer behandleJournalConsumer;

    @Test
    public void opprettJournalpost() {
        when(behandleJournalV2.journalfoerInngaaendeHenvendelse(any())).thenReturn(new WSJournalfoerInngaaendeHenvendelseResponse().withJournalpostId("id"));
        String id = behandleJournalConsumer.opprettJournalpost("fnr", "saksId", LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1));

        assertThat(id).isEqualTo("id");
    }

}
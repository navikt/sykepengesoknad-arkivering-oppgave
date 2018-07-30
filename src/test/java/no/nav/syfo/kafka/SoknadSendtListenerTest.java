package no.nav.syfo.kafka;

import no.nav.syfo.service.SaksbehandlingsService;
import no.nav.syfo.util.Toggle;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SoknadSendtListenerTest {

    @Mock
    private SaksbehandlingsService saksbehandlingsService;

    @InjectMocks
    private SoknadSendtListener soknadSendtListener;

    @Test
    public void skipperSaksbehandlingOmToggleErAv() throws Exception {
        Toggle.skipSaksbehandling = true;
        soknadSendtListener.listen(new ConsumerRecord<>("topic", 0, 0, "key", "value"));
        verify(saksbehandlingsService, never()).behandleSoknad(any());
    }
}
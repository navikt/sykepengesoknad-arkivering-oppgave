package no.nav.syfo.kafka;

import no.nav.syfo.config.unleash.FeatureToggle;
import no.nav.syfo.config.unleash.Toggle;
import no.nav.syfo.service.SaksbehandlingsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SoknadSendtListenerTest {

    @Mock
    private SaksbehandlingsService saksbehandlingsService;

    @Mock
    private Toggle toggle;

    @InjectMocks
    private SoknadSendtListener soknadSendtListener;

    @Test
    public void skipperSaksbehandlingOmToggleErAv() throws Exception {
        when(toggle.isEnabled(FeatureToggle.SKAL_LESE_SOKNADER_FRA_KOE)).thenReturn(false);
        soknadSendtListener.listen(new ConsumerRecord<>("topic", 0, 0, "key", "value"));
        verify(saksbehandlingsService, never()).behandleSoknad(any());
    }
}
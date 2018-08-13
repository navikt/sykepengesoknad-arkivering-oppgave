package no.nav.syfo.config.unleash;

import no.finn.unleash.Unleash;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.syfo.config.unleash.FeatureToggle.SKAL_LESE_SOKNADER_FRA_KOE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ToggleImplTest {
    @Mock
    private Unleash unleash;

    @Test
    public void isEnabledPEndepunkterForSoknad() {
        Toggle toggle = new ToggleImpl(unleash, "p");
        when(unleash.isEnabled(anyString())).thenReturn(true);
        assertThat(toggle.isEnabled(SKAL_LESE_SOKNADER_FRA_KOE)).isTrue();
        verify(unleash).isEnabled(anyString());
    }

    @Test
    public void isDisabledPEndepunkterForSoknad() {
        Toggle toggle = new ToggleImpl(unleash, "p");
        when(unleash.isEnabled(anyString())).thenReturn(false);
        assertThat(toggle.isEnabled(SKAL_LESE_SOKNADER_FRA_KOE)).isFalse();
        verify(unleash).isEnabled(anyString());
    }
}

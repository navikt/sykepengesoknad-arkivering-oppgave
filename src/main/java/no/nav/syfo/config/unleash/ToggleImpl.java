package no.nav.syfo.config.unleash;

import no.finn.unleash.Unleash;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("remote")
@Primary
public class ToggleImpl implements Toggle {
    private Unleash unleash;
    private boolean isProd;

    public ToggleImpl(Unleash unleash,
                      @Value("${fasit.environment.name:p}") String fasitEnvironmentName) {
        this.unleash = unleash;
        this.isProd = "p".equals(fasitEnvironmentName);
    }

    @Override
    public boolean isEnabled(FeatureToggle toggle) {
        if (this.isProd && !toggle.isAvailableInProd()) {
            return false;
        }
        return unleash.isEnabled(toggle.getToggleName());
    }
}

package no.nav.syfo.config.unleash;

import no.nav.syfo.config.unleash.strategy.ByEnvironmentStrategy;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static java.lang.String.join;
import static java.util.Collections.singletonMap;
import static java.util.stream.Stream.of;

@Service
public class ToggleMock implements Toggle {
    private ToggleMockConfig toggleMockConfig;
    private ByEnvironmentStrategy byEnvironmentStrategy;

    public ToggleMock(ToggleMockConfig toggleMockConfig,
                      ByEnvironmentStrategy byEnvironmentStrategy) {
        this.toggleMockConfig = toggleMockConfig;
        this.byEnvironmentStrategy = byEnvironmentStrategy;
    }

    @Override
    public boolean isEnabled(FeatureToggle toggle) {
        ToggleMockConfig.Config config = toggleMockConfig.getConfig().get(toggle);

        Boolean check = of(
                checkEnvironment(config)
                //Add more checks here!! N/A if null is returned!
        )
                .filter(Objects::nonNull)
                .max(Boolean::compareTo)
                .orElse(true);


        return checkEnabled(config) && check;
    }

    private boolean checkEnabled(ToggleMockConfig.Config config) {
        return config.isEnabled();
    }

    private Boolean checkEnvironment(ToggleMockConfig.Config config) {
        if (config.getByEnvironment() != null) {
            return byEnvironmentStrategy.isEnabled(singletonMap("miljø", join(",", config.getByEnvironment())));
        }
        return null;
    }
}

package no.nav.syfo.config.unleash;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Component
@ConfigurationProperties(prefix = "toggle")
public class ToggleMockConfig {
    private Map<FeatureToggle, Config> config;

    public Map<FeatureToggle, Config> getConfig() {
        if (config == null) {
            config = new EnumMap<>(FeatureToggle.class);
        }
        return config;
    }

    public static class Config {
        private boolean enabled;
        private List<String> byEnvironment;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getByEnvironment() {
            return byEnvironment;
        }

        public void setByEnvironment(List<String> byEnvironment) {
            this.byEnvironment = byEnvironment;
        }
    }
}

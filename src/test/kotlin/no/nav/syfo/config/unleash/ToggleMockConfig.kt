package no.nav.syfo.config.unleash

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*

@Component
@ConfigurationProperties(prefix = "toggle")
class ToggleMockConfig {
    private var config: Map<FeatureToggle, Config> = EnumMap(FeatureToggle::class.java)

    fun getConfig(): Map<FeatureToggle, Config> {
        return config
    }

    class Config {
        var isEnabled: Boolean = false
        var byEnvironment: List<String>? = null
    }
}

package no.nav.syfo.config.unleash

import no.finn.unleash.Unleash
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("remote")
@Primary
class ToggleImpl(private val unleash: Unleash,
                 @Value("\${fasit.environment.name:p}") fasitEnvironmentName: String) : Toggle {
    private val isProd: Boolean

    init {
        this.isProd = (fasitEnvironmentName == "p")
    }

    override fun isEnabled(toggle: FeatureToggle): Boolean {
        return  if (this.isProd && !toggle.isAvailableInProd)
                    false
                else
                    unleash.isEnabled(toggle.toggleName)
    }
}
package no.nav.syfo.config.unleash

interface Toggle {
    fun isEnabled(toggle: FeatureToggle): Boolean
}

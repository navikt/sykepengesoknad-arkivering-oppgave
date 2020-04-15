package no.nav.syfo.config.unleash

interface Toggle {
    fun isEnabled(toggle: FeatureToggle): Boolean
    fun isNotProduction(): Boolean
    fun isProduction(): Boolean
    fun isQ(): Boolean
}

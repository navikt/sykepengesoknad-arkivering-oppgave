package no.nav.syfo.config.unleash

enum class FeatureToggle constructor( val toggleName: String, val isAvailableInProd: Boolean) {
    SKAL_LESE_SOKNADER_FRA_KOE("syfo.syfogsak.skalLeseSoknaderFraKoe", true)
}

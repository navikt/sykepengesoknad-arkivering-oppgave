package no.nav.syfo.config.unleash;

public enum FeatureToggle {
    SKAL_LESE_SOKNADER_FRA_KOE("syfo.syfogsak.skalLeseSoknaderFraKoe", true);

    private final String toggleName;
    private final boolean availableInProd;

    FeatureToggle(String toggleName, boolean availableInProd) {
        this.toggleName = toggleName;
        this.availableInProd = availableInProd;
    }

    public String getToggleName() {
        return toggleName;
    }

    public boolean isAvailableInProd() {
        return availableInProd;
    }
}

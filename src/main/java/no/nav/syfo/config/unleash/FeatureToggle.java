package no.nav.syfo.config.unleash;

public enum FeatureToggle {
    TOGGLE_FOR_PROD("syfo.syfosoknad.toggleForProd", true),
    TOGGLE_IKKE_FOR_PROD("syfo.syfosoknad.toggleIkkeForProd", false);

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

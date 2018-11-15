package no.nav.syfo.domain.dto;

public enum Arbeidssituasjon {
    NAERINGSDRIVENDE ("selvstendig næringsdrivende"),
    FRILANSER ("frilanser"),
    ARBEIDSTAKER ("arbeidstaker");

    private final String name;

    Arbeidssituasjon(final String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}

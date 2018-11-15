package no.nav.syfo.domain.dto;

public enum PDFTemplate {
    ARBEIDSTAKERE("arbeidstakere"),
    SELVSTENDIGNAERINGSDRIVENDE("selvstendignaeringsdrivende"),
    SYKEPENGERUTLAND("sykepengerutland");

    private String endpoint;

    PDFTemplate(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return this.endpoint;
    }
}

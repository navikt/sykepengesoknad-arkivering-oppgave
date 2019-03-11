package no.nav.syfo.domain.dto

enum class PDFTemplate(val endpoint: String) {
    ARBEIDSTAKERE("arbeidstakere"),
    SELVSTENDIGNAERINGSDRIVENDE("selvstendignaeringsdrivende"),
    SYKEPENGERUTLAND("sykepengerutland");

    override fun toString(): String {
        return this.endpoint
    }
}

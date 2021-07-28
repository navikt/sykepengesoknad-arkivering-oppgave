package no.nav.syfo.domain.dto

enum class PDFTemplate(val endpoint: String) {
    ARBEIDSTAKERE("arbeidstakere"),
    SELVSTENDIGNAERINGSDRIVENDE("selvstendignaeringsdrivende"),
    SYKEPENGERUTLAND("sykepengerutland"),
    ARBEIDSLEDIG("arbeidsledig"),
    BEHANDLINGSDAGER("behandlingsdager"),
    ANNETARBEIDSFORHOLD("annetarbeidsforhold"),
    REISETILSKUDD("reisetilskudd"),
    GRADERT_REISETILSKUDD("gradertreisetilskudd");
}

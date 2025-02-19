package no.nav.helse.flex.domain.dto

enum class PDFTemplate(val endpoint: String) {
    ARBEIDSTAKERE("arbeidstakere"),
    SELVSTENDIGNAERINGSDRIVENDE("selvstendignaeringsdrivende"),
    SYKEPENGERUTLAND("sykepengerutland"),
    ARBEIDSLEDIG("arbeidsledig"),
    FRISKMELDT_TIL_ARBEIDSFORMIDLING("friskmeldt"),
    BEHANDLINGSDAGER("behandlingsdager"),
    ANNETARBEIDSFORHOLD("annetarbeidsforhold"),
    REISETILSKUDD("reisetilskudd"),
    GRADERT_REISETILSKUDD("gradertreisetilskudd"),
}

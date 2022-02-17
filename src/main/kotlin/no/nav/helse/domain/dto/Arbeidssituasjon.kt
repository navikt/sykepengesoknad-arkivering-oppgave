package no.nav.helse.domain.dto

enum class Arbeidssituasjon(var navn: String) {
    NAERINGSDRIVENDE("selvstendig næringsdrivende"),
    FRILANSER("frilanser"),
    ARBEIDSTAKER("arbeidstaker"),
    ARBEIDSLEDIG("arbeidsledig"),
    ANNET("annet");
}

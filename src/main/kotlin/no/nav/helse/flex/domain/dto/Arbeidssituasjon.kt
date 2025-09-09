package no.nav.helse.flex.domain.dto

enum class Arbeidssituasjon(
    var navn: String,
) {
    NAERINGSDRIVENDE("selvstendig næringsdrivende"),
    FRILANSER("frilanser"),
    BARNEPASSER(" barnepasser"),
    ARBEIDSTAKER("arbeidstaker"),
    ARBEIDSLEDIG("arbeidsledig"),
    FISKER("fisker"),
    JORDBRUKER("jordbruker"),
    ANNET("annet"),
}

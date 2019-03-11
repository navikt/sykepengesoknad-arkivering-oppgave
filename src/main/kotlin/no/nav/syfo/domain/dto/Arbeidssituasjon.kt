package no.nav.syfo.domain.dto

enum class Arbeidssituasjon(var navn: String) {
    NAERINGSDRIVENDE("selvstendig næringsdrivende"),
    FRILANSER("frilanser"),
    ARBEIDSTAKER("arbeidstaker");

    override fun toString(): String {
        return navn
    }
}

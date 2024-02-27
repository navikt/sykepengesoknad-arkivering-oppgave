package no.nav.helse.flex.domain.dto

fun List<Sporsmal>.harInntektsopplysninger(): Boolean {
    return this.any { it.tag.contains("INNTEKTSOPPLYSNINGER") }
}

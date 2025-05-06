package no.nav.helse.flex.domain.dto

fun List<Sporsmal>.harInntektsopplysninger(): Boolean = this.any { it.tag.contains("INNTEKTSOPPLYSNINGER") }

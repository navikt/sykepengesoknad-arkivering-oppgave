package no.nav.helse.flex.domain.dto

data class KjentInntektskilde(
    val navn: String,
    val kilde: Kilde,
    val orgnummer: String,
)

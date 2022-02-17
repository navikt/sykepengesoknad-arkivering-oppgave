package no.nav.helse.domain

import java.time.LocalDate

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate
)

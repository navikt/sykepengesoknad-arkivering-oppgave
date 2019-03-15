package no.nav.syfo.domain

import java.time.LocalDate


data class Periode(
        val fom: LocalDate? = null,
        val tom: LocalDate? = null
)

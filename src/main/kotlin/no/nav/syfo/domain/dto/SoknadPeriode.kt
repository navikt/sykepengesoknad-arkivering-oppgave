package no.nav.syfo.domain.dto

import java.time.LocalDate

data class SoknadPeriode(
        val fom: LocalDate? = null,
        val tom: LocalDate? = null,
        val grad: Int? = null,
        val faktiskGrad: Int? = null,
        val avtaltTimer: Double? = null,
        val faktiskTimer: Double? = null,
        val sykmeldingstype: String? = null
)

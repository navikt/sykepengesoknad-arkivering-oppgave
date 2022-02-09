package no.nav.syfo.domain

import java.time.LocalDate

data class Innsending(
    val id: String,
    val sykepengesoknadId: String,
    val journalpostId: String? = null,
    val oppgaveId: String? = null,
    val behandlet: LocalDate? = null,
    val soknadFom: LocalDate? = null,
    val soknadTom: LocalDate? = null
)

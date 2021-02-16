package no.nav.syfo.domain

import java.time.LocalDate

data class Innsending(
    val innsendingsId: String,
    val ressursId: String,
    val aktorId: String? = null,
    val saksId: String? = null,
    val journalpostId: String? = null,
    val oppgaveId: String? = null,
    val behandlet: LocalDate? = null,
    val soknadFom: LocalDate? = null,
    val soknadTom: LocalDate? = null
)

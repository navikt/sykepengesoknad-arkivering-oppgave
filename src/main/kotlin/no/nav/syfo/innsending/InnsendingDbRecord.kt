package no.nav.syfo.innsending

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("innsending")
data class InnsendingDbRecord(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val sykepengesoknadId: String? = null,
    val journalpostId: String? = null,
    val oppgaveId: String? = null,
    val behandlet: LocalDateTime? = null,
)

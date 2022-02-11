package no.nav.syfo.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface OppgaveRepository : CrudRepository<OppgaveDbRecord, String> {
    fun findBySykepengesoknadId(sykepengesoknadId: String): OppgaveDbRecord?
}

@Table("oppgavestyring")
data class OppgaveDbRecord(
    @Id
    val id: String? = null,
    val sykepengesoknadId: String,
    val status: OppgaveStatus,
    val opprettet: LocalDateTime = LocalDateTime.now(),
    val modifisert: LocalDateTime = LocalDateTime.now(),
    val timeout: LocalDateTime? = null,
    val avstemt: Boolean? = false
)

enum class OppgaveStatus {
    Utsett, Opprett, IkkeOpprett, Opprettet, OpprettSpeilRelatert
}

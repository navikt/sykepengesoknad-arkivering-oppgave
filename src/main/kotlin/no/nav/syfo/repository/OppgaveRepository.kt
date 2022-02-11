package no.nav.syfo.repository

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface OppgaveRepository : CrudRepository<OppgaveDbRecord, String> {
    fun findBySykepengesoknadId(sykepengesoknadId: String): OppgaveDbRecord?

    @Query("""select * from oppgavestyring where avstemt = true and (status = 'Opprett' or status = 'OpprettSpeilRelatert' OR (status = 'Utsett' AND timeout < now()))""")
    fun findOppgaverTilOpprettelse(): List<OppgaveDbRecord>

    @Modifying
    @Query("""update oppgavestyring set timeout = :timeout, status = :status where sykepengesoknad_id = :sykepengesoknadId""")
    fun updateOppgaveBySykepengesoknadId(sykepengesoknadId: String, timeout: LocalDateTime?, status: OppgaveStatus): Boolean

    @Modifying
    @Query("""update oppgavestyring set avstemt = true where sykepengesoknad_id = :sykepengesoknadId""")
    fun updateAvstemtBySykepengesoknadId(sykepengesoknadId: String): Boolean

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

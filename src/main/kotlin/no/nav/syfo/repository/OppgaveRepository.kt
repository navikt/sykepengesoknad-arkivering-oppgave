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

    @Modifying
    @Query("""update oppgavestyring set timeout = :timeout, status = :status where sykepengesoknad_id = :sykepengesoknadId""")
    fun updateOppgaveBySykepengesoknadId(sykepengesoknadId: String, timeout: LocalDateTime?, status: OppgaveStatus): Boolean

    @Modifying
    @Query("""delete from oppgavestyring where sykepengesoknad_id = :sykepengesoknadId""")
    fun deleteOppgaveBySykepengesoknadId(sykepengesoknadId: String): Long

    @Modifying
    @Query("""delete from oppgavestyring where opprettet < :foreldet""")
    fun deleteGamleOppgaver(): Long

    @Modifying
    @Query("""update oppgavestyring set avstemt = true where id = :id""")
    fun updateAvstem(id: String): Boolean
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

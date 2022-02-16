package no.nav.syfo.repository

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface SpreOppgaveRepository : CrudRepository<SpreOppgaveDbRecord, String> {
    fun findBySykepengesoknadId(sykepengesoknadId: String): SpreOppgaveDbRecord?

    @Query("""select * from oppgavestyring where avstemt = true and (status = 'Opprett' or status = 'OpprettSpeilRelatert' OR (status = 'Utsett' AND timeout < now()))""")
    fun findOppgaverTilOpprettelse(): List<SpreOppgaveDbRecord>

    @Modifying
    @Query("""update oppgavestyring set timeout = :timeout, status = :status where sykepengesoknad_id = :sykepengesoknadId""")
    fun updateOppgaveBySykepengesoknadId(sykepengesoknadId: String, timeout: Instant?, status: OppgaveStatus): Boolean

    @Modifying
    @Query("""update oppgavestyring set avstemt = true where sykepengesoknad_id = :sykepengesoknadId""")
    fun updateAvstemtBySykepengesoknadId(sykepengesoknadId: String): Boolean

    @Modifying
    @Query("""delete from oppgavestyring where sykepengesoknad_id = :sykepengesoknadId""")
    fun deleteOppgaveBySykepengesoknadId(sykepengesoknadId: String): Long

    @Modifying
    @Query("""delete from oppgavestyring where opprettet < (now() - interval '90 days')""")
    fun deleteGamleOppgaver(): Long
}

@Table("oppgavestyring")
data class SpreOppgaveDbRecord(
    @Id
    val id: String? = null,
    val sykepengesoknadId: String,
    val status: OppgaveStatus,
    val opprettet: Instant = Instant.now(),
    val modifisert: Instant = Instant.now(),
    val timeout: Instant? = null,
    val avstemt: Boolean? = false
)

enum class OppgaveStatus {
    Utsett, Opprett, IkkeOpprett, Opprettet, OpprettSpeilRelatert
}

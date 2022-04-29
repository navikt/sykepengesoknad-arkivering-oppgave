package no.nav.helse.flex.oppgavefordeling

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface OppgavefordelingRepository : CrudRepository<OppgavefordelingDbRecord, String> {

    fun findBySykepengesoknadId(sykepengesoknadId: String): OppgavefordelingDbRecord?

    @Modifying
    @Query("""INSERT INTO oppgavefordeling(sykepengesoknad_id, status) VALUES (:sykepengesoknadId, :status)""")
    fun insert(sykepengesoknadId: String, status: OppgavefordelingStatus)

    @Modifying
    @Query("""UPDATE oppgavefordeling SET avstemt = TRUE WHERE sykepengesoknad_id = :sykepengesoknadId""")
    fun settTilAvstemt(sykepengesoknadId: String): Boolean

    @Query("""SELECT f.* FROM oppgavefordeling f INNER JOIN innsending i ON f.sykepengesoknad_id = i.sykepengesoknad_id WHERE f.avstemt = true AND i.oppgave_id IS NULL LIMIT 100""")
    fun finnOppgaverSomMangler(): List<OppgavefordelingDbRecord>

    @Modifying
    @Query("""UPDATE oppgavefordeling SET ferdig = TRUE WHERE sykepengesoknad_id = :sykepengesoknadId""")
    fun settTilFerdig(sykepengesoknadId: String)
}

@Table("oppgavefordeling")
data class OppgavefordelingDbRecord(
    @Id
    val sykepengesoknadId: String,
    val status: OppgavefordelingStatus,
    val avstemt: Boolean = false,
    val sendtNav: Instant? = null,
    val ferdig: Boolean = false,
)

enum class OppgavefordelingStatus {
    LagOppgave, LagOppgaveForSpeilsaksbehandlere
}

package no.nav.helse.flex.oppgavefordeling

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDate

@Repository
interface OppgavefordelingRepository : CrudRepository<OppgavefordelingDbRecord, String> {
    fun findBySykepengesoknadId(sykepengesoknadId: String): OppgavefordelingDbRecord?

    @Modifying
    @Query("""INSERT INTO oppgavefordeling(sykepengesoknad_id, status) VALUES (:sykepengesoknadId, :status)""")
    fun insert(sykepengesoknadId: String, status: OppgavefordelingStatus)

    @Query(
        """
        SELECT *
        FROM oppgavefordeling a
          INNER JOIN innsending b ON b.sykepengesoknad_id = a.sykepengesoknad_id
        WHERE b.oppgave_id IS NULL
          AND a.avstemt IS TRUE
          AND a.kommune IS NULL 
          AND a.bydel IS NULL 
          AND a.land IS NULL 
        LIMIT 100
        """
    )
    fun hent100UtenGT(): List<OppgavefordelingDbRecord>

    @Modifying
    @Query(
        """
        UPDATE oppgavefordeling 
        SET kommune = :kommune,
            bydel = :bydel,
            land = :land
        WHERE sykepengesoknad_id = :sykepengesoknadId
        """
    )
    fun lagreGeografiskTilknytning(sykepengesoknadId: String, kommune: String?, bydel: String?, land: String?)

    fun findFnrBySykepengesoknadId(sykepengesoknadId: String): String
}

@Table("oppgavefordeling")
data class OppgavefordelingDbRecord(
    @Id
    val sykepengesoknadId: String,
    val status: OppgavefordelingStatus,
    val fnr: String? = null,
    val timeout: Instant? = null,
    val avstemt: Boolean = false,
    val sendtNav: Instant? = null,
    val sendtArbeidsgiver: Instant? = null,
    val korrigertAv: String? = null,
    val kommune: String? = null,
    val bydel: String? = null,
    val land: String? = null,
    val fom: LocalDate? = null,
    val tom: LocalDate? = null,
)

enum class OppgavefordelingStatus {
    LagOppgave, LagOppgaveForSpeilsaksbehandlere
}

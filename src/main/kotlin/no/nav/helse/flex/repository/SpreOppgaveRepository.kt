package no.nav.helse.flex.repository

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

    @Query(
        """
        SELECT *
        FROM   oppgavestyring
        WHERE  avstemt = true
          AND (status = 'Opprett'
               OR status = 'OpprettSpeilRelatert'
               OR (status = 'Utsett' AND timeout < :timeout)
               OR (status = 'VenterPaBomlo' AND timeout < :timeout))
        """,
    )
    fun findOppgaverTilOpprettelse(timeout: Instant): List<SpreOppgaveDbRecord>

    @Modifying
    @Query(
        """
        UPDATE oppgavestyring 
        SET timeout = :timeout, 
            status = :status,
            modifisert = :modifisert
        WHERE sykepengesoknad_id = :sykepengesoknadId
        """,
    )
    fun updateOppgaveBySykepengesoknadId(
        sykepengesoknadId: String,
        timeout: Instant?,
        status: OppgaveStatus,
        modifisert: Instant,
    ): Boolean

    @Modifying
    @Query("""UPDATE oppgavestyring SET avstemt = TRUE WHERE sykepengesoknad_id = :sykepengesoknadId""")
    fun updateAvstemtBySykepengesoknadId(sykepengesoknadId: String): Boolean

    @Modifying
    @Query("""DELETE FROM oppgavestyring WHERE sykepengesoknad_id = :sykepengesoknadId""")
    fun deleteOppgaveBySykepengesoknadId(sykepengesoknadId: String): Long
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
    val avstemt: Boolean? = false,
)

enum class OppgaveStatus {
    Utsett,
    VenterPaBomlo,
    IkkeOpprett,
    Opprett,
    OpprettSpeilRelatert,
    Opprettet,
    OpprettetSpeilRelatert,
    OpprettetTimeout,
}

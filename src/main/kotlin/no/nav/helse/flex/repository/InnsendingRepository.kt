package no.nav.helse.flex.repository

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface InnsendingRepository : CrudRepository<InnsendingDbRecord, String> {
    fun findBySykepengesoknadId(sykepengesoknadId: String): InnsendingDbRecord?

    @Modifying
    @Query(""" UPDATE innsending SET behandlet = now() WHERE id = :id """)
    fun updateBehandlet(id: String): Boolean

    @Modifying
    @Query("""UPDATE innsending SET oppgave_id = :oppgaveId WHERE id = :id""")
    fun updateOppgaveId(id: String, oppgaveId: String): Boolean

    @Modifying
    @Query("""UPDATE innsending SET journalpost_id = :journalpostId WHERE id = :id""")
    fun updateJournalpostId(id: String, journalpostId: String): Boolean
}

@Table("innsending")
data class InnsendingDbRecord(
    @Id
    val id: String? = null,
    val sykepengesoknadId: String,
    val journalpostId: String? = null,
    val oppgaveId: String? = null,
    val behandlet: Instant? = null
)

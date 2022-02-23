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
    @Query("""update innsending set behandlet = current_timestamp where id = :id""")
    fun updateBehandlet(id: String): Boolean

    @Modifying
    @Query("""update innsending set behandlet = :behandlet where sykepengesoknad_id = :sykepengesoknadId and behandlet is null""")
    fun updateBehandletBySykepengesoknadid(sykepengesoknadId: String, behandlet: Instant): Boolean

    @Modifying
    @Query("""update innsending set oppgave_id = :oppgaveId where id = :id""")
    fun updateOppgaveId(id: String, oppgaveId: String): Boolean

    @Modifying
    @Query("""update innsending set journalpost_id = :journalpostId where id = :id""")
    fun updateJournalpostId(id: String, journalpostId: String): Boolean
}

@Table("innsending")
data class InnsendingDbRecord(
    @Id
    val id: String? = null,
    val sykepengesoknadId: String,
    val journalpostId: String? = null,
    val oppgaveId: String? = null,
    val behandlet: Instant? = null,
)

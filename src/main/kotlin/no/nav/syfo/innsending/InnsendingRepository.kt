package no.nav.syfo.innsending

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface InnsendingRepository : CrudRepository<InnsendingDbRecord, String> {
    fun findBySykepengesoknadId(sykepengesoknadId: String): InnsendingDbRecord?
    @Modifying
    @Query("UPDATE innsending SET behandlet = CURRENT_TIMESTAMP WHERE id = :id")
    fun updateBehandlet(id: String): InnsendingDbRecord?
    @Modifying
    @Query("UPDATE innsending SET oppgave_id = :oppgaveId WHERE id = :id")
    fun updateOppgaveId(id: String, oppgaveId: String): InnsendingDbRecord?
    @Modifying
    @Query("UPDATE innsending SET journalpost_id = :journalpostId WHERE id = :id")
    fun updateJournalpostId(id: String, journalpostId: String): InnsendingDbRecord?
}

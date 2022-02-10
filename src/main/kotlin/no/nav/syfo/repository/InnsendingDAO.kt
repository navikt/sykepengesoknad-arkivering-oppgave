package no.nav.syfo.repository

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Repository
class InnsendingDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    fun oppdaterJournalpostId(uuid: String, journalpostId: String) {
        namedParameterJdbcTemplate.update(
            "UPDATE INNSENDING SET JOURNALPOST_ID = :journalpostId WHERE ID = :uuid",
            MapSqlParameterSource()
                .addValue("journalpostId", journalpostId)
                .addValue("uuid", uuid)
        )
    }

    fun oppdaterOppgaveId(uuid: String, oppgaveId: String) {
        namedParameterJdbcTemplate.update(
            "UPDATE INNSENDING SET OPPGAVE_ID = :oppgaveId WHERE ID = :uuid",
            MapSqlParameterSource()
                .addValue("oppgaveId", oppgaveId)
                .addValue("uuid", uuid)
        )
    }

    fun settBehandlet(uuid: String) {
        namedParameterJdbcTemplate.update(
            "UPDATE INNSENDING SET BEHANDLET = CURRENT_TIMESTAMP WHERE ID = :uuid",
            MapSqlParameterSource()
                .addValue("uuid", uuid)
        )
    }
}

package no.nav.syfo.repository

import no.nav.syfo.domain.Innsending
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
@Repository
class InnsendingDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    fun opprettInnsending(
        sykepengesoknadId: String,
        soknadFom: LocalDate?,
        soknadTom: LocalDate?
    ): String {
        val uuid = UUID.randomUUID().toString()

        namedParameterJdbcTemplate.update(
            "INSERT INTO INNSENDING (ID, SYKEPENGESOKNAD_ID, SOKNAD_FOM, SOKNAD_TOM) VALUES (:id, :sykepengesoknadId, :fom, :tom)",
            MapSqlParameterSource()
                .addValue("id", uuid)
                .addValue("sykepengesoknadId", sykepengesoknadId)
                .addValue("fom", soknadFom)
                .addValue("tom", soknadTom)
        )

        return uuid
    }

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

    fun finnInnsendingForSykepengesoknad(sykepengesoknadId: String): Innsending? {
        return namedParameterJdbcTemplate.query(
            "SELECT * FROM INNSENDING WHERE SYKEPENGESOKNAD_ID = :sykepengesoknadId",
            MapSqlParameterSource().addValue("sykepengesoknadId", sykepengesoknadId),
            innsendingRowMapper
        ).firstOrNull()
    }
}

val innsendingRowMapper: (ResultSet, Int) -> Innsending = { resultSet, _ ->
    Innsending(
        id = resultSet.getString("ID"),
        sykepengesoknadId = resultSet.getString("SYKEPENGESOKNAD_ID"),
        journalpostId = resultSet.getString("JOURNALPOST_ID"),
        oppgaveId = resultSet.getString("OPPGAVE_ID"),
        behandlet = resultSet.getDate("BEHANDLET")?.toLocalDate(),
        soknadFom = resultSet.getDate("SOKNAD_FOM")?.toLocalDate(),
        soknadTom = resultSet.getDate("SOKNAD_TOM")?.toLocalDate()
    )
}

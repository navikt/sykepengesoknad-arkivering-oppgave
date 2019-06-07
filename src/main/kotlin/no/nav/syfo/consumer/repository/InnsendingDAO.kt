package no.nav.syfo.consumer.repository

import no.nav.syfo.domain.Innsending
import no.nav.syfo.log
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

    val log = log()

    fun opprettInnsending(
        sykepengesoknadId: String,
        aktorId: String,
        soknadFom: LocalDate?,
        soknadTom: LocalDate?
    ): String {
        val uuid = UUID.randomUUID().toString()

        namedParameterJdbcTemplate.update(
            "INSERT INTO INNSENDING (INNSENDING_UUID, RESSURS_ID, AKTOR_ID, SOKNAD_FOM, SOKNAD_TOM) VALUES (:uuid, :ressursId, :aktorId, :fom, :tom)",
            MapSqlParameterSource()
                .addValue("uuid", uuid)
                .addValue("ressursId", sykepengesoknadId)
                .addValue("aktorId", aktorId)
                .addValue("fom", soknadFom)
                .addValue("tom", soknadTom)
        )

        return uuid
    }

    fun oppdaterAktorId(uuid: String, aktorId: String) {
        namedParameterJdbcTemplate.update(
            "UPDATE INNSENDING SET AKTOR_ID = :aktorId WHERE INNSENDING_UUID = :uuid",
            MapSqlParameterSource()
                .addValue("aktorId", aktorId)
                .addValue("uuid", uuid)
        )
    }

    fun oppdaterSaksId(uuid: String, saksId: String) {
        namedParameterJdbcTemplate.update(
            "UPDATE INNSENDING SET SAKS_ID = :saksId WHERE INNSENDING_UUID = :uuid",
            MapSqlParameterSource()
                .addValue("saksId", saksId)
                .addValue("uuid", uuid)
        )
    }

    fun oppdaterJournalpostId(uuid: String, journalpostId: String) {
        namedParameterJdbcTemplate.update(
            "UPDATE INNSENDING SET JOURNALPOST_ID = :journalpostId WHERE INNSENDING_UUID = :uuid",
            MapSqlParameterSource()
                .addValue("journalpostId", journalpostId)
                .addValue("uuid", uuid)
        )
    }

    fun oppdaterOppgaveId(uuid: String, oppgaveId: String) {
        namedParameterJdbcTemplate.update(
            "UPDATE INNSENDING SET OPPGAVE_ID = :oppgaveId WHERE INNSENDING_UUID = :uuid",
            MapSqlParameterSource()
                .addValue("oppgaveId", oppgaveId)
                .addValue("uuid", uuid)
        )
    }

    fun settBehandlet(uuid: String) {
        namedParameterJdbcTemplate.update(
            "UPDATE INNSENDING SET BEHANDLET = CURRENT_TIMESTAMP WHERE INNSENDING_UUID = :uuid",
            MapSqlParameterSource()
                .addValue("uuid", uuid)
        )
    }

    fun leggTilFeiletInnsending(uuid: String) {
        namedParameterJdbcTemplate.update(
            "INSERT INTO FEILET_INNSENDING (INNSENDING_UUID, TIDSPUNKT) VALUES(:uuid, CURRENT_TIMESTAMP)",
            MapSqlParameterSource()
                .addValue("uuid", uuid)
        )
    }

    fun finnInnsendingForSykepengesoknad(sykepengesoknadId: String): Innsending? {
        return namedParameterJdbcTemplate.query(
            "SELECT * FROM INNSENDING WHERE RESSURS_ID = :ressursId",
            MapSqlParameterSource()
                .addValue("ressursId", sykepengesoknadId),

            innsendingRowMapper
        ).firstOrNull()
    }

    fun hentFeilendeInnsendinger(): List<Innsending> {
        return namedParameterJdbcTemplate.query(
            "SELECT * " +
                    "FROM INNSENDING " +
                    "WHERE INNSENDING_UUID IN (SELECT INNSENDING_UUID FROM FEILET_INNSENDING)",

            innsendingRowMapper
        )
    }

    fun fjernFeiletInnsending(innsendingsId: String) {
        namedParameterJdbcTemplate.update(
            "DELETE FROM FEILET_INNSENDING WHERE INNSENDING_UUID = :innsendingsId",

            MapSqlParameterSource().addValue("innsendingsId", innsendingsId)
        )
    }

    fun finnTidligereInnsendinger(aktorId: String): List<TidligereInnsending> {
        return namedParameterJdbcTemplate.query(
            """
                    SELECT * FROM INNSENDING WHERE AKTOR_ID = :aktorId
                    AND SAKS_ID IS NOT NULL
                    AND BEHANDLET IS NOT NULL
                    AND SOKNAD_FOM IS NOT NULL
                    AND SOKNAD_TOM IS NOT NULL
                    """,
            MapSqlParameterSource()
                .addValue("aktorId", aktorId),

            tidligereInnsendingRowMapper
        )
    }
}

data class TidligereInnsending(
    val aktorId: String,
    val saksId: String,
    val behandlet: LocalDate,
    val soknadFom: LocalDate,
    val soknadTom: LocalDate
)

val tidligereInnsendingRowMapper: (ResultSet, Int) -> TidligereInnsending = { resultSet, _ ->
    TidligereInnsending(
        aktorId = resultSet.getString("AKTOR_ID"),
        saksId = resultSet.getString("SAKS_ID"),
        behandlet = resultSet.getDate("BEHANDLET").toLocalDate(),
        soknadFom = resultSet.getDate("SOKNAD_FOM").toLocalDate(),
        soknadTom = resultSet.getDate("SOKNAD_TOM").toLocalDate()
    )
}

val innsendingRowMapper: (ResultSet, Int) -> Innsending = { resultSet, _ ->
    Innsending(
        innsendingsId = resultSet.getString("INNSENDING_UUID"),
        ressursId = resultSet.getString("RESSURS_ID"),
        aktorId = resultSet.getString("AKTOR_ID"),
        saksId = resultSet.getString("SAKS_ID"),
        journalpostId = resultSet.getString("JOURNALPOST_ID"),
        oppgaveId = resultSet.getString("OPPGAVE_ID"),
        behandlet = resultSet.getDate("BEHANDLET")?.toLocalDate(),
        soknadFom = resultSet.getDate("SOKNAD_FOM")?.toLocalDate(),
        soknadTom = resultSet.getDate("SOKNAD_TOM")?.toLocalDate()
    )
}

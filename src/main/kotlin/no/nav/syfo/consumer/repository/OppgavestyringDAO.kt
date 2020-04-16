package no.nav.syfo.consumer.repository

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDateTime

@Service
@Transactional
@Repository
class OppgavestyringDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    fun nySøknad(søknadsId: String, timeout: LocalDateTime) {
        namedParameterJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT) values (:soknadsId, :timeout)",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId)
                .addValue("timeout", timeout)
        )
    }

    fun fjernSøknad(søknadsId: String) {
        namedParameterJdbcTemplate.update(
            "DELETE FROM OPPGAVESTYRING WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId)
        )
    }

    fun hentSøknad(søknadsId: String): Oppgavestyring? {
        return namedParameterJdbcTemplate.query(
            "SELECT * FROM OPPGAVESTYRING WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId),
            oppgavestyringRowMapper
        ).firstOrNull()
    }

    fun oppdaterTimeout(søknadsId: String, timeout: LocalDateTime) {
        namedParameterJdbcTemplate.update(
            "UPDATE OPPGAVESTYRING SET TIMEOUT = :timeout WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId)
                .addValue("timeout", timeout)
        )
    }
}

data class Oppgavestyring(
    val søknadsId: String,
    val timeout: LocalDateTime
)

val oppgavestyringRowMapper: (ResultSet, Int) -> Oppgavestyring = { resultSet, _ ->
    Oppgavestyring(
        søknadsId = resultSet.getString("SYKEPENGESOKNAD_ID"),
        timeout = resultSet.getObject("TIMEOUT", LocalDateTime::class.java)
    )
}

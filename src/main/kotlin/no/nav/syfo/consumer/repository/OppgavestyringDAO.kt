package no.nav.syfo.consumer.repository


import no.nav.syfo.log
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

    val log = log()

    fun nySpreOppgave(søknadsId: String, timeout: LocalDateTime) {
        log.info("Oppretter ny SpreOppgave for id $søknadsId og timeout $timeout")
        namedParameterJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT) values (:soknadsId, :timeout)",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId)
                .addValue("timeout", timeout)
        )
    }

    fun fjernSpreOppgave(søknadsId: String) {
        log.info("Fjerner SpreOppgave for id $søknadsId")
        namedParameterJdbcTemplate.update(
            "DELETE FROM OPPGAVESTYRING WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId)
        )
    }

    fun hentSpreOppgave(søknadsId: String): SpreOppgave? {
        return namedParameterJdbcTemplate.query(
            "SELECT * FROM OPPGAVESTYRING WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId),
            oppgavestyringRowMapper
        ).firstOrNull()
    }

    fun oppdaterTimeout(søknadsId: String, timeout: LocalDateTime) {
        log.info("Endrer timeout til $timeout for id $søknadsId")
        namedParameterJdbcTemplate.update(
            "UPDATE OPPGAVESTYRING SET TIMEOUT = :timeout WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId)
                .addValue("timeout", timeout)
        )
    }
}

data class SpreOppgave(
    val søknadsId: String,
    val timeout: LocalDateTime
)

val oppgavestyringRowMapper: (ResultSet, Int) -> SpreOppgave = { resultSet, _ ->
    SpreOppgave(
        søknadsId = resultSet.getString("SYKEPENGESOKNAD_ID"),
        timeout = resultSet.getObject("TIMEOUT", LocalDateTime::class.java)
    )
}

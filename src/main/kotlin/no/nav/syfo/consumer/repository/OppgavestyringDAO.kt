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

    fun nySpreOppgave(søknadsId: String, timeout: LocalDateTime?, status: OppgaveStatus) {
        log.info("Oppretter ny SpreOppgave for id $søknadsId og timeout $timeout")
        val opprettet = LocalDateTime.now()
        namedParameterJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT) values (:soknadsId, :timeout, :status, :opprettet, :modifisert)",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId)
                .addValue("timeout", timeout)
                .addValue("status", status.toString())
                .addValue("opprettet", opprettet)
                .addValue("modifisert", opprettet)
        )
    }

    fun settStatus(søknadsId: String, status: OppgaveStatus) {
        log.info("Oppdaterer status for $søknadsId til $status")
        namedParameterJdbcTemplate.update(
            "UPDATE OPPGAVESTYRING SET STATUS = :status, MODIFISERT = :modifisert WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("status", status.toString())
                .addValue("soknadsId", søknadsId)
                .addValue("modifisert", LocalDateTime.now())
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

    fun hentOppgaverTilOpprettelse(): List<SpreOppgave> {
        return namedParameterJdbcTemplate.query(
            "SELECT * FROM OPPGAVESTYRING WHERE STATUS = 'OPPRETT' OR (STATUS = 'UTSETT' AND TIMEOUT < :now)",
            MapSqlParameterSource()
                .addValue("now", LocalDateTime.now()),
            oppgavestyringRowMapper
        )
    }

    fun settTimeout(søknadsId: String, timeout: LocalDateTime?) {
        log.info("Endrer timeout til $timeout for id $søknadsId")
        namedParameterJdbcTemplate.update(
            "UPDATE OPPGAVESTYRING SET TIMEOUT = :timeout, MODIFISERT = :oppdatert WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId)
                .addValue("timeout", timeout)
                .addValue("oppdatert", LocalDateTime.now())
        )
    }
}

enum class OppgaveStatus {
    Utsett, Opprett, IkkeOpprett, Opprettet
}

data class SpreOppgave(
    val søknadsId: String,
    val timeout: LocalDateTime?,
    val status: OppgaveStatus,
    val opprettet: LocalDateTime,
    val modifisert: LocalDateTime
)

val oppgavestyringRowMapper: (ResultSet, Int) -> SpreOppgave = { resultSet, _ ->
    SpreOppgave(
        søknadsId = resultSet.getString("SYKEPENGESOKNAD_ID"),
        timeout = resultSet.getObject("TIMEOUT", LocalDateTime::class.java),
        status = OppgaveStatus.valueOf(resultSet.getString("STATUS")),
        opprettet = resultSet.getObject("OPPRETTET", LocalDateTime::class.java),
        modifisert = resultSet.getObject("MODIFISERT", LocalDateTime::class.java)
    )
}

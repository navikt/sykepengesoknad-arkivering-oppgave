package no.nav.syfo.consumer.repository

import no.nav.syfo.logger
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
@Repository
class OppgavestyringDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    private val log = logger()

    fun nySpreOppgave(søknadsId: UUID, timeout: LocalDateTime?, status: OppgaveStatus, avstemt: Boolean = false) {
        log.info("Oppretter ny SpreOppgave for id $søknadsId og timeout $timeout")
        val opprettet = LocalDateTime.now()
        namedParameterJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT, AVSTEMT) values (:soknadsId, :timeout, :status, :opprettet, :modifisert, :avstemt)",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId.toString())
                .addValue("timeout", timeout)
                .addValue("status", status.name)
                .addValue("opprettet", opprettet)
                .addValue("modifisert", opprettet)
                .addValue("avstemt", if (avstemt) 1 else 0)
        )
    }

    fun slettSpreOppgave(søknadsId: String) {
        namedParameterJdbcTemplate.update(
            "DELETE FROM OPPGAVESTYRING WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId)
        )
    }

    fun slettGamleSpreOppgaver(): Int {
        return namedParameterJdbcTemplate.update(
            "DELETE FROM OPPGAVESTYRING WHERE OPPRETTET < :foreldet",
            MapSqlParameterSource()
                .addValue("foreldet", LocalDateTime.now().minusMonths(3))
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
            "SELECT * FROM OPPGAVESTYRING WHERE AVSTEMT = 1 AND (STATUS = 'Opprett' OR (STATUS = 'Utsett' AND TIMEOUT < :now))",
            MapSqlParameterSource()
                .addValue("now", LocalDateTime.now()),
            oppgavestyringRowMapper
        )
    }

    fun oppdaterOppgave(søknadsId: UUID, timeout: LocalDateTime?, status: OppgaveStatus) {
        log.info("Oppdaterer SpreOppgave på søknad: $søknadsId med verdier: timeout: $timeout og status: ${status.name}")
        namedParameterJdbcTemplate.update(
            "UPDATE OPPGAVESTYRING SET STATUS = :status, MODIFISERT = :modifisert, timeout = :timeout WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("status", status.name)
                .addValue("timeout", timeout)
                .addValue("soknadsId", søknadsId.toString())
                .addValue("modifisert", LocalDateTime.now())
        )
    }

    fun avstem(søknadsId: String) {
        namedParameterJdbcTemplate.update(
            "UPDATE OPPGAVESTYRING SET AVSTEMT = 1 WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId)
        )
        log.info("Avstemte oppgave på søknad: $søknadsId")
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
    val modifisert: LocalDateTime,
    val avstemt: Boolean
)

val oppgavestyringRowMapper: (ResultSet, Int) -> SpreOppgave = { resultSet, _ ->
    SpreOppgave(
        søknadsId = resultSet.getString("SYKEPENGESOKNAD_ID"),
        timeout = resultSet.getObject("TIMEOUT", LocalDateTime::class.java),
        status = OppgaveStatus.valueOf(resultSet.getString("STATUS")),
        opprettet = resultSet.getObject("OPPRETTET", LocalDateTime::class.java),
        modifisert = resultSet.getObject("MODIFISERT", LocalDateTime::class.java),
        avstemt = resultSet.getInt("AVSTEMT") == 1
    )
}

package no.nav.syfo.repository

import no.nav.syfo.logger
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

    private val log = logger()

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

    fun hentOppgaverTilOpprettelse(): List<OppgaveDbRecord> {
        return namedParameterJdbcTemplate.query(
            "SELECT * FROM OPPGAVESTYRING WHERE AVSTEMT = true AND (STATUS = 'Opprett' OR STATUS = 'OpprettSpeilRelatert' OR (STATUS = 'Utsett' AND TIMEOUT < :now))",
            MapSqlParameterSource()
                .addValue("now", LocalDateTime.now()),
            oppgavestyringRowMapper
        )
    }

    fun avstem(søknadsId: String) {
        namedParameterJdbcTemplate.update(
            "UPDATE OPPGAVESTYRING SET AVSTEMT = true WHERE SYKEPENGESOKNAD_ID = :soknadsId",
            MapSqlParameterSource()
                .addValue("soknadsId", søknadsId)
        )
        log.info("Avstemte oppgave på søknad: $søknadsId")
    }
}

val oppgavestyringRowMapper: (ResultSet, Int) -> OppgaveDbRecord = { resultSet, _ ->
    OppgaveDbRecord(
        sykepengesoknadId = resultSet.getString("SYKEPENGESOKNAD_ID"),
        timeout = resultSet.getTimestamp("TIMEOUT")?.toLocalDateTime(),
        status = OppgaveStatus.valueOf(resultSet.getString("STATUS")),
        opprettet = resultSet.getTimestamp("OPPRETTET").toLocalDateTime(),
        modifisert = resultSet.getTimestamp("MODIFISERT").toLocalDateTime(),
        avstemt = resultSet.getBoolean("AVSTEMT")
    )
}

package no.nav.syfo.consumer.repository

import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
@Repository
class OppgavestyringLogDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    fun loggEvent(
        oppgave: OppgaveDTO
    ): Int {
        val opprettet = LocalDateTime.now()
        val keyHolder = GeneratedKeyHolder()

        namedParameterJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING_LOG (SYKEPENGESOKNAD_ID, OPPDATERINGSTYPE, OPPRETTET_DATO, TIMEOUT) VALUES (:sykepengesoknadId, :oppdateringstype, :opprettetDato, :timeout)",
            MapSqlParameterSource()
                .addValue("sykepengesoknadId", oppgave.dokumentId)
                .addValue("oppdateringstype", oppgave.oppdateringstype.toString())
                .addValue("opprettetDato", opprettet)
                .addValue("timeout", oppgave.timeout),
            keyHolder,
            listOf("ID").toTypedArray()
        )

        return keyHolder.key!!.toInt()
    }

    fun hentEventerForSykepengesoknadId(id: String): List<OppgaveDTO>  {
        return namedParameterJdbcTemplate.query(
            "SELECT * FROM OPPGAVESTYRING_LOG WHERE SYKEPENGESOKNAD_ID = :id",
            MapSqlParameterSource()
                .addValue("id", id),
            oppgavestyringLogRowMapper
        )
    }
}

val oppgavestyringLogRowMapper: (ResultSet, Int) -> OppgaveDTO = { resultSet, _ ->
    OppgaveDTO(
        dokumentId = UUID.fromString(resultSet.getString("SYKEPENGESOKNAD_ID")),
        timeout = resultSet.getObject("TIMEOUT", LocalDateTime::class.java),
        dokumentType = DokumentTypeDTO.Søknad,
        oppdateringstype = OppdateringstypeDTO.valueOf(resultSet.getString("OPPDATERINGSTYPE"))
    )
}

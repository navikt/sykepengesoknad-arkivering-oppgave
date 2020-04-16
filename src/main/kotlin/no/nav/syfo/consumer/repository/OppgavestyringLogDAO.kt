package no.nav.syfo.consumer.repository

import no.nav.syfo.domain.OppgaveDTO
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

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
}

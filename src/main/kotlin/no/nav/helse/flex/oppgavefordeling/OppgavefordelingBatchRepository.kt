package no.nav.helse.flex.oppgavefordeling

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class OppgavefordelingBatchRepository(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
) {

    fun settFomTom(liste: List<FomTom>) {
        val sql = """
            UPDATE oppgavefordeling 
            SET fom = :fom,
                tom = :tom
            WHERE sykepengesoknad_id = :sykepengesoknadId
            """

        val batchParams = SqlParameterSourceUtils.createBatch(liste)
        namedParameterJdbcTemplate.batchUpdate(sql, batchParams)
    }
}

data class FomTom(
    val sykepengesoknadId: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
)

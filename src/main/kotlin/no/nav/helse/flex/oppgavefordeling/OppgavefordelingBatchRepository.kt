package no.nav.helse.flex.oppgavefordeling

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
class OppgavefordelingBatchRepository(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun settSendtTilArbeidsGiver(arbeidsgiverDatoer: List<ArbeidsgiverDato>) {
        val sql = """
            UPDATE oppgavefordeling 
            SET sendt_arbeidsgiver = :sendtArbeidsgiver 
            WHERE sykepengesoknad_id = :sykepengesoknadId
            """

        val batchParams = SqlParameterSourceUtils.createBatch(arbeidsgiverDatoer)
        namedParameterJdbcTemplate.batchUpdate(sql, batchParams)
    }
}

data class ArbeidsgiverDato(
    val sykepengesoknadId: String,
    val sendArbeidsgiver: Timestamp
)

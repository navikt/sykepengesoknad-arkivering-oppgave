package no.nav.helse.flex.oppgavefordeling

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Repository

@Repository
class OppgavefordelingBatchRepository(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
) {

    fun settFodselsnummer(fodselsnummere: List<Fodselsnummer>) {
        val sql = """
            UPDATE oppgavefordeling 
            SET fnr = :fodselsnummer 
            WHERE sykepengesoknad_id = :sykepengesoknadId
            """

        val batchParams = SqlParameterSourceUtils.createBatch(fodselsnummere)
        namedParameterJdbcTemplate.batchUpdate(sql, batchParams)
    }
}

data class Fodselsnummer(
    val sykepengesoknadId: String,
    val fodselsnummer: String,
)

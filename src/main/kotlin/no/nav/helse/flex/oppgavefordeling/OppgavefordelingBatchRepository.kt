package no.nav.helse.flex.oppgavefordeling

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

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

data class ArbeidsgiverDato(
    val sykepengesoknadId: String,
    val sendtArbeidsgiver: OffsetDateTime
)

data class Fodselsnummer(
    val sykepengesoknadId: String,
    val fodselsnummer: String,
)

package no.nav.helse.flex.oppgavefordeling

import no.nav.helse.flex.logger
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class OppgavefordelingBatchRepository(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
) {

    private var log = logger()

    fun settSendtTilArbeidsGiver(arbeidsgiverDatoer: List<ArbeidsgiverDato>) {
        val sql = """
            UPDATE oppgavefordeling 
            SET sendt_arbeidsgiver = :sendtArbeidsgiver 
            WHERE sykepengesoknad_id = :sykepengesoknadId
            """

        val batchParams = SqlParameterSourceUtils.createBatch(arbeidsgiverDatoer)

        val batchUpdate = namedParameterJdbcTemplate.batchUpdate(sql, batchParams)
        log.info("Lagret ${batchUpdate.sum()} verdier med sendtTilArbeidsgiver.")
    }
}

data class ArbeidsgiverDato(
    val sykepengesoknadId: String,
    val sendtArbeidsgiver: OffsetDateTime
)

package no.nav.syfo.repository

import no.nav.syfo.logger
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
}

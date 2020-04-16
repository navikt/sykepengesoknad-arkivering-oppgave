package no.nav.syfo.consumer.repository

import no.nav.syfo.log
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
@Repository
class OppgavestyringDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {
    val log = log()


}

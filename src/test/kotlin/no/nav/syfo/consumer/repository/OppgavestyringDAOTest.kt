package no.nav.syfo.consumer.repository

import no.nav.syfo.TestApplication
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestApplication::class])
@EmbeddedKafka
@DirtiesContext
class OppgavestyringDAOTest {
    @Inject
    private lateinit var oppgavestyringDAO: OppgavestyringDAO
    @Inject
    private lateinit var namedParaJdbcTemplate: NamedParameterJdbcTemplate

    @Before
    fun setup() {
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT) values (:soknadsId, :timeout, :status, :opprettet, :modifisert)",
            MapSqlParameterSource()
                .addValue("soknadsId", "uuid")
                .addValue("timeout", null)
                .addValue("status", "Opprettet")
                .addValue("opprettet", LocalDateTime.now().minusDays(2))
                .addValue("modifisert", LocalDateTime.now().minusDays(2))
        )
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT) values (:soknadsId, :timeout, :status, :opprettet, :modifisert)",
            MapSqlParameterSource()
                .addValue("soknadsId", "uuid-1")
                .addValue("timeout", null)
                .addValue("status", "Opprettet")
                .addValue("opprettet", LocalDateTime.now().minusMonths(4))
                .addValue("modifisert", LocalDateTime.now().minusMonths(4).plusHours(2))
        )
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT) values (:soknadsId, :timeout, :status, :opprettet, :modifisert)",
            MapSqlParameterSource()
                .addValue("soknadsId", "uuid-2")
                .addValue("timeout", null)
                .addValue("status", "Opprettet")
                .addValue("opprettet", LocalDateTime.now().minusMonths(12))
                .addValue("modifisert", LocalDateTime.now().minusDays(2))
        )
    }

    @Test
    fun `sletter gamle oppgaver`() {
        oppgavestyringDAO.slettGamleSpreOppgaver()
        val oppgaver = hentAlleOppgaver()
        assertEquals(1, oppgaver.size)
        assertEquals("uuid", oppgaver[0].søknadsId)
    }

    private fun hentAlleOppgaver() =
        namedParaJdbcTemplate.query("SELECT * FROM OPPGAVESTYRING", oppgavestyringRowMapper)
}
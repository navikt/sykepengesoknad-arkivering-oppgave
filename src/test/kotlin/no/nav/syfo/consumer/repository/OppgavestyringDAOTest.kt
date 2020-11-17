package no.nav.syfo.consumer.repository

import no.nav.syfo.TestApplication
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestApplication::class])
@EmbeddedKafka
@DirtiesContext
class OppgavestyringDAOTest {
    @Autowired
    private lateinit var oppgavestyringDAO: OppgavestyringDAO
    @Autowired
    private lateinit var namedParaJdbcTemplate: NamedParameterJdbcTemplate
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Before
    fun setup() {
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT, AVSTEMT) values (:soknadsId, :timeout, :status, :opprettet, :modifisert, :avstemt)",
            MapSqlParameterSource()
                .addValue("soknadsId", "uuid")
                .addValue("timeout", null)
                .addValue("status", OppgaveStatus.Opprettet.name)
                .addValue("opprettet", LocalDateTime.now().minusDays(2))
                .addValue("modifisert", LocalDateTime.now().minusDays(2))
                .addValue("avstemt", 1)
        )
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT, AVSTEMT) values (:soknadsId, :timeout, :status, :opprettet, :modifisert, :avstemt)",
            MapSqlParameterSource()
                .addValue("soknadsId", "uuid-1")
                .addValue("timeout", null)
                .addValue("status", OppgaveStatus.Opprettet.name)
                .addValue("opprettet", LocalDateTime.now().minusMonths(4))
                .addValue("modifisert", LocalDateTime.now().minusMonths(4).plusHours(2))
                .addValue("avstemt", 1)
        )
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT, AVSTEMT) values (:soknadsId, :timeout, :status, :opprettet, :modifisert, :avstemt)",
            MapSqlParameterSource()
                .addValue("soknadsId", "uuid-2")
                .addValue("timeout", null)
                .addValue("status", OppgaveStatus.Opprettet.name)
                .addValue("opprettet", LocalDateTime.now().minusMonths(12))
                .addValue("modifisert", LocalDateTime.now().minusDays(2))
                .addValue("avstemt", 1)
        )
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT, AVSTEMT) values (:soknadsId, :timeout, :status, :opprettet, :modifisert, :avstemt)",
            MapSqlParameterSource()
                .addValue("soknadsId", "uuid-3")
                .addValue("timeout", LocalDateTime.now().minusHours(1))
                .addValue("status", OppgaveStatus.Utsett.name)
                .addValue("opprettet", LocalDateTime.now())
                .addValue("modifisert", LocalDateTime.now())
                .addValue("avstemt", 0)
        )
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT, AVSTEMT) values (:soknadsId, :timeout, :status, :opprettet, :modifisert, :avstemt)",
            MapSqlParameterSource()
                .addValue("soknadsId", "uuid-4")
                .addValue("timeout", LocalDateTime.now().minusHours(1))
                .addValue("status", OppgaveStatus.Utsett.name)
                .addValue("opprettet", LocalDateTime.now())
                .addValue("modifisert", LocalDateTime.now())
                .addValue("avstemt", 1)
        )
    }

    @After
    fun tearDown() {
        jdbcTemplate.update("DELETE FROM OPPGAVESTYRING")
    }

    @Test
    fun `sletter gamle oppgaver`() {
        oppgavestyringDAO.slettGamleSpreOppgaver()
        val oppgaver = hentAlleOppgaver()
        assertEquals(3, oppgaver.size)
        assertEquals("uuid", oppgaver[0].søknadsId)
        assertEquals("uuid-3", oppgaver[1].søknadsId)
        assertEquals("uuid-4", oppgaver[2].søknadsId)
    }

    @Test
    fun `henter oppgaver til opprettelse`() {
        val oppgaver = oppgavestyringDAO.hentOppgaverTilOpprettelse()
        assertEquals(1, oppgaver.size)
        assertEquals("uuid-4", oppgaver.first().søknadsId)
    }



    private fun hentAlleOppgaver() =
        namedParaJdbcTemplate.query("SELECT * FROM OPPGAVESTYRING", oppgavestyringRowMapper)
}

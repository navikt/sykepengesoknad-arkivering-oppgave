package no.nav.syfo.repository

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.TestApplication
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDateTime

@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
@EnableMockOAuth2Server
class OppgavestyringDAOTest : AbstractContainerBaseTest() {
    @Autowired
    private lateinit var oppgavestyringDAO: OppgavestyringDAO
    @Autowired
    private lateinit var namedParaJdbcTemplate: NamedParameterJdbcTemplate
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setup() {
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT, AVSTEMT) values (:sykepengesoknadId, :timeout, :status, :opprettet, :modifisert, :avstemt)",
            MapSqlParameterSource()
                .addValue("sykepengesoknadId", "uuid")
                .addValue("timeout", null)
                .addValue("status", OppgaveStatus.Opprettet.name)
                .addValue("opprettet", LocalDateTime.now().minusDays(2))
                .addValue("modifisert", LocalDateTime.now().minusDays(2))
                .addValue("avstemt", true)
        )
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT, AVSTEMT) values (:sykepengesoknadId, :timeout, :status, :opprettet, :modifisert, :avstemt)",
            MapSqlParameterSource()
                .addValue("sykepengesoknadId", "uuid-1")
                .addValue("timeout", null)
                .addValue("status", OppgaveStatus.Opprettet.name)
                .addValue("opprettet", LocalDateTime.now().minusMonths(4))
                .addValue("modifisert", LocalDateTime.now().minusMonths(4).plusHours(2))
                .addValue("avstemt", true)
        )
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT, AVSTEMT) values (:sykepengesoknadId, :timeout, :status, :opprettet, :modifisert, :avstemt)",
            MapSqlParameterSource()
                .addValue("sykepengesoknadId", "uuid-2")
                .addValue("timeout", null)
                .addValue("status", OppgaveStatus.Opprettet.name)
                .addValue("opprettet", LocalDateTime.now().minusMonths(12))
                .addValue("modifisert", LocalDateTime.now().minusDays(2))
                .addValue("avstemt", true)
        )
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT, AVSTEMT) values (:sykepengesoknadId, :timeout, :status, :opprettet, :modifisert, :avstemt)",
            MapSqlParameterSource()
                .addValue("sykepengesoknadId", "uuid-3")
                .addValue("timeout", LocalDateTime.now().minusHours(1))
                .addValue("status", OppgaveStatus.Utsett.name)
                .addValue("opprettet", LocalDateTime.now())
                .addValue("modifisert", LocalDateTime.now())
                .addValue("avstemt", false)
        )
        namedParaJdbcTemplate.update(
            "INSERT INTO OPPGAVESTYRING (SYKEPENGESOKNAD_ID, TIMEOUT, STATUS, OPPRETTET, MODIFISERT, AVSTEMT) values (:sykepengesoknadId, :timeout, :status, :opprettet, :modifisert, :avstemt)",
            MapSqlParameterSource()
                .addValue("sykepengesoknadId", "uuid-4")
                .addValue("timeout", LocalDateTime.now().minusHours(1))
                .addValue("status", OppgaveStatus.Utsett.name)
                .addValue("opprettet", LocalDateTime.now())
                .addValue("modifisert", LocalDateTime.now())
                .addValue("avstemt", true)
        )
    }

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("DELETE FROM OPPGAVESTYRING")
    }

    @Test
    fun `sletter gamle oppgaver`() {
        oppgavestyringDAO.slettGamleSpreOppgaver()
        val oppgaver = hentAlleOppgaver()
        assertEquals(3, oppgaver.size)
        assertEquals("uuid", oppgaver[0].sykepengesoknadId)
        assertEquals("uuid-3", oppgaver[1].sykepengesoknadId)
        assertEquals("uuid-4", oppgaver[2].sykepengesoknadId)
    }

    @Test
    fun `henter oppgaver til opprettelse`() {
        val oppgaver = oppgavestyringDAO.hentOppgaverTilOpprettelse()
        assertEquals(1, oppgaver.size)
        assertEquals("uuid-4", oppgaver.first().sykepengesoknadId)
    }

    private fun hentAlleOppgaver() =
        namedParaJdbcTemplate.query("SELECT * FROM OPPGAVESTYRING", oppgavestyringRowMapper)
}

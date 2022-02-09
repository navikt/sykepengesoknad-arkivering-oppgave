package no.nav.syfo.repository

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate

@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
@EnableMockOAuth2Server
class InnsendingDAOTest : AbstractContainerBaseTest() {

    @Autowired
    private lateinit var innsendingDAO: InnsendingDAO

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    @BeforeEach
    fun initDB() {
        cleanup()
    }

    @AfterEach
    fun cleanup() {
        jdbcTemplate.update("DELETE FROM INNSENDING")
    }

    @Test
    fun lagreInnsending() {
        val uuid = innsendingDAO.opprettInnsending(
            sykepengesoknadId = "soknad-uuid",
            soknadFom = LocalDate.of(2019, 3, 8),
            soknadTom = LocalDate.of(2019, 3, 20)
        )
        innsendingDAO.oppdaterJournalpostId(
            uuid = uuid,
            journalpostId = "journalpostId"
        )
        innsendingDAO.oppdaterOppgaveId(uuid, "oppgaveId")
        innsendingDAO.settBehandlet(uuid)

        val innsendinger = jdbcTemplate.query("SELECT * FROM INNSENDING", innsendingRowMapper)

        assertThat(innsendinger.size).isEqualTo(1)
        assertThat(innsendinger.get(0).id).isEqualTo(uuid)
        assertThat(innsendinger.get(0).sykepengesoknadId).isEqualTo("soknad-uuid")
        assertThat(innsendinger.get(0).journalpostId).isEqualTo("journalpostId")
        assertThat(innsendinger.get(0).oppgaveId).isEqualTo("oppgaveId")
        assertThat(innsendinger.get(0).behandlet).isEqualTo(LocalDate.now())
    }

    @Test
    fun sjekkOmInnsendingForSoknadAlleredeErLaget() {
        innsendingDAO.opprettInnsending(
            sykepengesoknadId = "soknad_123",
            soknadFom = LocalDate.of(2019, 3, 8),
            soknadTom = LocalDate.of(2019, 3, 20)
        )

        val innsending = innsendingDAO.finnInnsendingForSykepengesoknad("soknad_123")
        assertThat(innsending).isNotNull()

        val innsending2 = innsendingDAO.finnInnsendingForSykepengesoknad("soknad_1234")
        assertThat(innsending2).isNull()
    }
}

package no.nav.syfo.consumer.repository

import no.nav.syfo.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import javax.inject.Inject

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
class InnsendingDAOTest {

    @Inject
    private lateinit var innsendingDAO: InnsendingDAO
    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @Before
    fun initDB() {
        cleanup()
    }

    @After
    fun cleanup() {
        jdbcTemplate.update("DELETE FROM FEILET_INNSENDING")
        jdbcTemplate.update("DELETE FROM INNSENDING")
    }

    @Test
    fun lagreInnsending() {
        val uuid = innsendingDAO.opprettInnsending("soknad-uuid", "aktor")
        innsendingDAO.oppdaterSaksId(uuid, "saksId")
        innsendingDAO.oppdaterJournalpostId(uuid, "journalpostId")
        innsendingDAO.oppdaterOppgaveId(uuid, "oppgaveId")
        innsendingDAO.settBehandlet(uuid)

        val innsendinger = jdbcTemplate.query("SELECT * FROM INNSENDING", innsendingRowMapper)

        assertThat(innsendinger.size).isEqualTo(1)
        assertThat(innsendinger.get(0).innsendingsId).isEqualTo(uuid)
        assertThat(innsendinger.get(0).aktorId).isEqualTo("aktor")
        assertThat(innsendinger.get(0).ressursId).isEqualTo("soknad-uuid")
        assertThat(innsendinger.get(0).saksId).isEqualTo("saksId")
        assertThat(innsendinger.get(0).journalpostId).isEqualTo("journalpostId")
        assertThat(innsendinger.get(0).oppgaveId).isEqualTo("oppgaveId")
        assertThat(innsendinger.get(0).behandlet).isEqualTo(LocalDate.now())
    }

    @Test
    fun sjekkOmInnsendingForSoknadAlleredeErLaget() {
        innsendingDAO.opprettInnsending("soknad_123", "aktor")

        val innsending = innsendingDAO.finnInnsendingForSykepengesoknad("soknad_123")
        assertThat(innsending).isNotNull()

        val innsending2 = innsendingDAO.finnInnsendingForSykepengesoknad("soknad_1234")
        assertThat(innsending2).isNull()
    }

    @Test
    fun hentFeilendeInnsendinger() {
        jdbcTemplate.update("INSERT INTO INNSENDING(INNSENDING_UUID, RESSURS_ID, AKTOR_ID) VALUES ('UUID-1', 'RESSURSID-1', 'AKTOR-1')")
        jdbcTemplate.update("INSERT INTO INNSENDING(INNSENDING_UUID, RESSURS_ID, AKTOR_ID, SAKS_ID) VALUES ('UUID-4', 'RESSURSID-4', 'AKTOR-4', 'SAK-4')")
        jdbcTemplate.update("INSERT INTO INNSENDING VALUES ('UUID-2', 'RESSURSID-2', 'AKTORID-2', 'SAKSID-2', 'JOURNALPOSTID-2', 'OPPGAVEID-2', '2018-09-14')")
        jdbcTemplate.update("INSERT INTO INNSENDING VALUES ('UUID-3', 'RESSURSID-3', 'AKTORID-3', 'SAKSID-3', 'JOURNALPOSTID-3', 'OPPGAVEID-3', '2018-09-15')")

        jdbcTemplate.update("INSERT INTO FEILET_INNSENDING (INNSENDING_UUID, TIDSPUNKT) VALUES ('UUID-1', '2018-09-13')")
        jdbcTemplate.update("INSERT INTO FEILET_INNSENDING (INNSENDING_UUID, TIDSPUNKT) VALUES ('UUID-4', '2018-09-13')")

        val feilendeInnsendinger = innsendingDAO.hentFeilendeInnsendinger()

        assertThat(feilendeInnsendinger.size).isEqualTo(2)
        assertThat(feilendeInnsendinger[0].innsendingsId).isEqualTo("UUID-1")
        assertThat(feilendeInnsendinger[1].innsendingsId).isEqualTo("UUID-4")
    }

    @Test
    fun finnSisteSak() {
        jdbcTemplate.update("INSERT INTO INNSENDING(INNSENDING_UUID, RESSURS_ID, AKTOR_ID) VALUES ('UUID-1', 'RESSURSID-1', 'AKTORID-1')")
        jdbcTemplate.update("INSERT INTO INNSENDING(INNSENDING_UUID, RESSURS_ID, AKTOR_ID, SAKS_ID) VALUES ('UUID-4', 'RESSURSID-2', 'AKTORID-1', 'SAK-1')")
        jdbcTemplate.update("INSERT INTO INNSENDING VALUES ('UUID-2', 'RESSURSID-3', 'AKTORID-1', 'SAKSID-2', 'JOURNALPOSTID-2', 'OPPGAVEID-2', '2018-09-14')")
        jdbcTemplate.update("INSERT INTO INNSENDING VALUES ('UUID-3', 'RESSURSID-4', 'AKTORID-1', 'SAKSID-3', 'JOURNALPOSTID-2', 'OPPGAVEID-2', '2018-09-18')")
        jdbcTemplate.update("INSERT INTO INNSENDING VALUES ('UUID-5', 'RESSURSID-5', 'AKTORID-1', 'SAKSID-4', 'JOURNALPOSTID-2', 'OPPGAVEID-2', '2018-09-13')")

        val sisteSak = innsendingDAO.finnSisteSak("AKTORID-1")
        assertThat(sisteSak).isEqualTo("SAKSID-3")
    }

    @Test
    fun finnSisteSakErTom() {
        jdbcTemplate.update("INSERT INTO INNSENDING(INNSENDING_UUID, RESSURS_ID, AKTOR_ID) VALUES ('UUID-1', 'RESSURSID-1', 'AKTORID-1')")
        jdbcTemplate.update("INSERT INTO INNSENDING(INNSENDING_UUID, RESSURS_ID, AKTOR_ID, SAKS_ID) VALUES ('UUID-4', 'RESSURSID-2', 'AKTORID-1', 'SAK-1')")

        val sisteSak = innsendingDAO.finnSisteSak("AKTORID-1")
        assertThat(sisteSak).isNull()
    }
}


package no.nav.syfo.consumer.repository

import no.nav.syfo.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import java.util.UUID
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
        val uuid = innsendingDAO.opprettInnsending("soknad-uuid", "aktor", LocalDate.of(2019,3,8), LocalDate.of(2019,3,20))
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
        innsendingDAO.opprettInnsending("soknad_123", "aktor", LocalDate.of(2019, 3, 8), LocalDate.of(2019, 3, 20))

        val innsending = innsendingDAO.finnInnsendingForSykepengesoknad("soknad_123")
        assertThat(innsending).isNotNull()

        val innsending2 = innsendingDAO.finnInnsendingForSykepengesoknad("soknad_1234")
        assertThat(innsending2).isNull()
    }

    @Test
    fun hentFeilendeInnsendinger() {
        jdbcTemplate.update("INSERT INTO INNSENDING(INNSENDING_UUID, RESSURS_ID, AKTOR_ID) VALUES ('UUID-1', 'RESSURSID-1', 'AKTOR-1')")
        jdbcTemplate.update("INSERT INTO INNSENDING(INNSENDING_UUID, RESSURS_ID, AKTOR_ID, SAKS_ID) VALUES ('UUID-4', 'RESSURSID-4', 'AKTOR-4', 'SAK-4')")
        jdbcTemplate.update("INSERT INTO INNSENDING VALUES ('UUID-2', 'RESSURSID-2', 'AKTORID-2', 'SAKSID-2', 'JOURNALPOSTID-2', 'OPPGAVEID-2', '2018-09-14', null, null)")
        jdbcTemplate.update("INSERT INTO INNSENDING VALUES ('UUID-3', 'RESSURSID-3', 'AKTORID-3', 'SAKSID-3', 'JOURNALPOSTID-3', 'OPPGAVEID-3', '2018-09-15', null, null)")

        jdbcTemplate.update("INSERT INTO FEILET_INNSENDING (INNSENDING_UUID, TIDSPUNKT) VALUES ('UUID-1', '2018-09-13')")
        jdbcTemplate.update("INSERT INTO FEILET_INNSENDING (INNSENDING_UUID, TIDSPUNKT) VALUES ('UUID-4', '2018-09-13')")

        val feilendeInnsendinger = innsendingDAO.hentFeilendeInnsendinger()

        assertThat(feilendeInnsendinger.size).isEqualTo(2)
        assertThat(feilendeInnsendinger[0].innsendingsId).isEqualTo("UUID-1")
        assertThat(feilendeInnsendinger[1].innsendingsId).isEqualTo("UUID-4")
    }
}

fun NamedParameterJdbcTemplate.insertBehandletSoknad(
    uuid: String? = UUID.randomUUID().toString(),
    ressursId: String? = UUID.randomUUID().toString(),
    aktor: String? = "aktor",
    saksId: String? = "saksId",
    journalpostId: String? = "journalpostId",
    oppgaveId: String? = "oppgaveId",
    behandlet: LocalDate? = LocalDate.now(),
    soknadFom: LocalDate? = LocalDate.now().minusDays(10),
    soknadTom: LocalDate? = LocalDate.now()
) {
    update(
        "INSERT INTO INNSENDING(INNSENDING_UUID, RESSURS_ID, AKTOR_ID, SAKS_ID, JOURNALPOST_ID, OPPGAVE_ID, BEHANDLET, SOKNAD_FOM, SOKNAD_TOM) VALUES (:uuid, :ressursId, :aktor, :saksId, :journalpostId, :oppgaveId, :behandlet, :soknadFom, :soknadTom)",
        MapSqlParameterSource()
            .addValue("uuid", uuid)
            .addValue("ressursId", ressursId)
            .addValue("aktor", aktor)
            .addValue("saksId", saksId)
            .addValue("journalpostId", journalpostId)
            .addValue("oppgaveId", oppgaveId)
            .addValue("behandlet", behandlet)
            .addValue("soknadFom", soknadFom)
            .addValue("soknadTom", soknadTom)
    )
}

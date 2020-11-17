package no.nav.syfo.consumer.repository

import no.nav.syfo.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
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
import java.time.LocalDate
import java.util.UUID

@RunWith(SpringRunner::class)
@EmbeddedKafka
@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
class InnsendingDAOTest {

    @Autowired
    private lateinit var innsendingDAO: InnsendingDAO
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate
    @Autowired
    private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    @Before
    fun initDB() {
        cleanup()
    }

    @After
    fun cleanup() {
        jdbcTemplate.update("DELETE FROM INNSENDING")
    }

    @Test
    fun lagreInnsending() {
        val uuid =
            innsendingDAO.opprettInnsending("soknad-uuid", "aktor", LocalDate.of(2019, 3, 8), LocalDate.of(2019, 3, 20))
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


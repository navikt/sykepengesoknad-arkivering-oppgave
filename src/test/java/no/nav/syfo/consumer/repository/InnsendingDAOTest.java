package no.nav.syfo.consumer.repository;

import no.nav.syfo.TestApplication;
import no.nav.syfo.domain.Innsending;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@DirtiesContext
public class InnsendingDAOTest {

    @Inject
    private InnsendingDAO innsendingDAO;
    @Inject
    private JdbcTemplate jdbcTemplate;

    @Before
    public void initDB() {
        cleanup();
    }

    @After
    public void cleanup() {
        jdbcTemplate.update("DELETE FROM FEILET_INNSENDING");
        jdbcTemplate.update("DELETE FROM INNSENDING");
    }

    @Test
    public void lagreInnsending() {
        String uuid = innsendingDAO.opprettInnsending("soknad-uuid", "aktor");
        innsendingDAO.oppdaterSaksId(uuid, "saksId");
        innsendingDAO.oppdaterJournalpostId(uuid, "journalpostId");
        innsendingDAO.oppdaterOppgaveId(uuid, "oppgaveId");
        innsendingDAO.settBehandlet(uuid);

        List<Innsending> innsendinger = jdbcTemplate.query("SELECT * FROM INNSENDING", InnsendingDAO.getInnsendingRowMapper());

        assertThat(innsendinger.size()).isEqualTo(1);
        assertThat(innsendinger.get(0).getInnsendingsId()).isEqualTo(uuid);
        assertThat(innsendinger.get(0).getAktorId()).isEqualTo("aktor");
        assertThat(innsendinger.get(0).getRessursId()).isEqualTo("soknad-uuid");
        assertThat(innsendinger.get(0).getSaksId()).isEqualTo("saksId");
        assertThat(innsendinger.get(0).getJournalpostId()).isEqualTo("journalpostId");
        assertThat(innsendinger.get(0).getOppgaveId()).isEqualTo("oppgaveId");
        assertThat(innsendinger.get(0).getBehandlet()).isEqualTo(LocalDate.now());
    }

    @Test
    public void sjekkOmInnsendingForSoknadAlleredeErLaget() {
        innsendingDAO.opprettInnsending("soknad_123", "aktor");

        Optional<Innsending> innsending = innsendingDAO.finnInnsendingForSykepengesoknad("soknad_123");
        assertThat(innsending.isPresent()).isTrue();

        Optional<Innsending> innsending2 = innsendingDAO.finnInnsendingForSykepengesoknad("soknad_1234");
        assertThat(innsending2.isPresent()).isFalse();
    }

    @Test
    public void hentFeilendeInnsendinger() {
        jdbcTemplate.update("INSERT INTO INNSENDING(INNSENDING_UUID, RESSURS_ID, AKTOR_ID) VALUES ('UUID-1', 'RESSURSID-1', 'AKTOR-1')");
        jdbcTemplate.update("INSERT INTO INNSENDING(INNSENDING_UUID, RESSURS_ID, AKTOR_ID, SAKS_ID) VALUES ('UUID-4', 'RESSURSID-4', 'AKTOR-4', 'SAK-4')");
        jdbcTemplate.update("INSERT INTO INNSENDING VALUES ('UUID-2', 'RESSURSID-2', 'AKTORID-2', 'SAKSID-2', 'JOURNALPOSTID-2', 'OPPGAVEID-2', '2018-09-14')");
        jdbcTemplate.update("INSERT INTO INNSENDING VALUES ('UUID-3', 'RESSURSID-3', 'AKTORID-3', 'SAKSID-3', 'JOURNALPOSTID-3', 'OPPGAVEID-3', '2018-09-15')");

        jdbcTemplate.update("INSERT INTO FEILET_INNSENDING (INNSENDING_UUID, TIDSPUNKT) VALUES ('UUID-1', '2018-09-13')");
        jdbcTemplate.update("INSERT INTO FEILET_INNSENDING (INNSENDING_UUID, TIDSPUNKT) VALUES ('UUID-4', '2018-09-13')");

        List<Innsending> feilendeInnsendinger = innsendingDAO.hentFeilendeInnsendinger();

        assertThat(feilendeInnsendinger.size()).isEqualTo(2);
        assertThat(feilendeInnsendinger.get(0).getInnsendingsId()).isEqualTo("UUID-1");
        assertThat(feilendeInnsendinger.get(1).getInnsendingsId()).isEqualTo("UUID-4");
    }
}
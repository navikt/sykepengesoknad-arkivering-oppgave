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
        jdbcTemplate.update("DELETE FROM INNSENDING");
    }

    @Test
    public void lagreInnsending() {
        String uuid = innsendingDAO.opprettInnsending("soknad-uuid");
        innsendingDAO.oppdaterAktorId(uuid, "aktor");
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
}
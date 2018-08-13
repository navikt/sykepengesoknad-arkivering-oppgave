package no.nav.syfo.consumer.repository;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Innsending;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@Transactional
@Repository
public class InnsendingDAO {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public InnsendingDAO(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public String opprettInnsending(String ressursId, String aktørId) {
        String uuid = UUID.randomUUID().toString();

        namedParameterJdbcTemplate.update(
                "INSERT INTO INNSENDING (INNSENDING_UUID, RESSURS_ID, AKTOR_ID) VALUES (:uuid, :ressursId, :aktorId)",
                new MapSqlParameterSource()
                        .addValue("uuid", uuid)
                        .addValue("ressursId", ressursId)
                        .addValue("aktorId", aktørId)
        );

        return uuid;
    }

    public void oppdaterSaksId(String uuid, String saksId) {
        namedParameterJdbcTemplate.update(
                "UPDATE INNSENDING SET SAKS_ID = :saksId WHERE INNSENDING_UUID = :uuid",
                new MapSqlParameterSource()
                    .addValue("saksId", saksId)
                    .addValue("uuid", uuid)
        );
    }

    public void oppdaterJournalpostId(String uuid, String journalpostId) {
        namedParameterJdbcTemplate.update(
                "UPDATE INNSENDING SET JOURNALPOST_ID = :journalpostId WHERE INNSENDING_UUID = :uuid",
                new MapSqlParameterSource()
                        .addValue("journalpostId", journalpostId)
                        .addValue("uuid", uuid)
        );
    }

    public void oppdaterOppgaveId(String uuid, String oppgaveId) {
        namedParameterJdbcTemplate.update(
                "UPDATE INNSENDING SET OPPGAVE_ID = :oppgaveId WHERE INNSENDING_UUID = :uuid",
                new MapSqlParameterSource()
                        .addValue("oppgaveId", oppgaveId)
                        .addValue("uuid", uuid)
        );
    }

    public void settBehandlet(String uuid) {
        namedParameterJdbcTemplate.update(
                "UPDATE INNSENDING SET BEHANDLET = now() WHERE INNSENDING_UUID = :uuid",
                new MapSqlParameterSource()
                        .addValue("uuid", uuid)
        );
    }

    public void leggTilFeiletInnsending(String uuid) {
        namedParameterJdbcTemplate.update(
                "INSERT INTO FEILET_INNSENDING VALUES(:uuid)",
                new MapSqlParameterSource()
                        .addValue("uuid", uuid)
        );
    }

    public static RowMapper<Innsending> getInnsendingRowMapper() {
        return (resultSet, i) -> Innsending.builder()
                .innsendingsId(resultSet.getString("INNSENDING_UUID"))
                .ressursId(resultSet.getString("RESSURS_ID"))
                .aktørId(resultSet.getString("AKTOR_ID"))
                .saksId(resultSet.getString("SAKS_ID"))
                .journalpostId(resultSet.getString("JOURNALPOST_ID"))
                .oppgaveId(resultSet.getString("OPPGAVE_ID"))
                .behandlet(resultSet.getDate("BEHANDLET").toLocalDate())
                .build();
    }
}

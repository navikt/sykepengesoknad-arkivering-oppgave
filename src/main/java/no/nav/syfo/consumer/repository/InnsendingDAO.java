package no.nav.syfo.consumer.repository;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Innsending;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
@Transactional
@Repository
public class InnsendingDAO {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public InnsendingDAO(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public String opprettInnsending(String sykepengesoknadId) {
        String uuid = UUID.randomUUID().toString();

        namedParameterJdbcTemplate.update(
                "INSERT INTO INNSENDING (INNSENDING_UUID, RESSURS_ID) VALUES (:uuid, :ressursId)",
                new MapSqlParameterSource()
                        .addValue("uuid", uuid)
                        .addValue("ressursId", sykepengesoknadId)
        );

        return uuid;
    }

    public void oppdaterAktorId(String uuid, String aktorId) {
        namedParameterJdbcTemplate.update(
                "UPDATE INNSENDING SET AKTOR_ID = :aktorId WHERE INNSENDING_UUID = :uuid",
                new MapSqlParameterSource()
                        .addValue("aktorId", aktorId)
                        .addValue("uuid", uuid)
        );
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
                "UPDATE INNSENDING SET BEHANDLET = CURRENT_TIMESTAMP WHERE INNSENDING_UUID = :uuid",
                new MapSqlParameterSource()
                        .addValue("uuid", uuid)
        );
    }

    public void leggTilFeiletInnsending(String uuid) {
        namedParameterJdbcTemplate.update(
                "INSERT INTO FEILET_INNSENDING (INNSENDING_UUID, TIDSPUNKT) VALUES(:uuid, CURRENT_TIMESTAMP)",
                new MapSqlParameterSource()
                        .addValue("uuid", uuid)
        );
    }

    public Innsending finnInnsendingForSykepengesoknad(String sykepengesoknadId) {
        return namedParameterJdbcTemplate.query(
                "SELECT * FROM INNSENDING WHERE RESSURS_ID = :ressursId",
                new MapSqlParameterSource()
                        .addValue("ressursId", sykepengesoknadId),
                getInnsendingRowMapper()
        ).stream()
                .findFirst()
                .orElse(null);
    }

    public static RowMapper<Innsending> getInnsendingRowMapper() {
        return (resultSet, i) -> Innsending.builder()
                .innsendingsId(resultSet.getString("INNSENDING_UUID"))
                .ressursId(resultSet.getString("RESSURS_ID"))
                .aktorId(resultSet.getString("AKTOR_ID"))
                .saksId(resultSet.getString("SAKS_ID"))
                .journalpostId(resultSet.getString("JOURNALPOST_ID"))
                .oppgaveId(resultSet.getString("OPPGAVE_ID"))
                .behandlet(ofNullable(resultSet.getDate("BEHANDLET"))
                        .map(Date::toLocalDate)
                        .orElse(null)
                )
                .build();
    }
}

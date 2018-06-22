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

    public String lagreInnsending(final Innsending innsending) {
        String uuid = UUID.randomUUID().toString();

        namedParameterJdbcTemplate.update(
                "INSERT INTO INNSENDING VALUES(:uuid, :ressursId, :aktorId, :saksId, :journalpostId, :oppgaveId, :behandlet)",
                new MapSqlParameterSource()
                        .addValue("uuid", uuid)
                        .addValue("aktorId", innsending.getAktørId())
                        .addValue("ressursId", innsending.getRessursId())
                        .addValue("saksId", innsending.getSaksId())
                        .addValue("journalpostId", innsending.getJournalpostId())
                        .addValue("oppgaveId", innsending.getOppgaveId())
                        .addValue("behandlet", innsending.getBehandlet())
        );

        return uuid;
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

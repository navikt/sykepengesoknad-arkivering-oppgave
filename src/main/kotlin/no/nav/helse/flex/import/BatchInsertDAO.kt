package no.nav.helse.flex.import

import no.nav.helse.flex.repository.InnsendingDbRecord
import no.nav.helse.flex.repository.SpreOppgaveDbRecord
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.Timestamp

@Repository
class BatchInsertDAO(
    private val jdbcTemplate: JdbcTemplate
) {

    fun batchInsertInnsending(records: List<InnsendingDbRecord>): IntArray {

        val sql = """
INSERT INTO INNSENDING 
    ("sykepengesoknad_id",
    "journalpost_id",
    "oppgave_id",
    "behandlet")
VALUES
  (?,?,?,?);"""

        return jdbcTemplate.batchUpdate(
            sql,
            object : BatchPreparedStatementSetter {
                override fun setValues(ps: PreparedStatement, i: Int) {
                    ps.setString(1, records[i].sykepengesoknadId)
                    ps.setString(2, records[i].journalpostId)
                    ps.setString(3, records[i].oppgaveId)
                    ps.setTimestamp(
                        4,
                        if (records[i].behandlet == null) {
                            null
                        } else {
                            Timestamp.from(records[i].behandlet)
                        }
                    )
                }

                override fun getBatchSize() = records.size
            }
        )
    }

    fun batchInsertSpreOppgave(records: List<SpreOppgaveDbRecord>): IntArray {

        val sql = """
INSERT INTO  OPPGAVESTYRING 
    ("sykepengesoknad_id",
    "status",
    "opprettet",
    "modifisert",
    "timeout",
    "avstemt")
VALUES
  (?,?,?,?,?,?) ON CONFLICT ON CONSTRAINT oppgavestyring_sykepengesoknad_id_key DO NOTHING;"""

        return jdbcTemplate.batchUpdate(
            sql,
            object : BatchPreparedStatementSetter {
                override fun setValues(ps: PreparedStatement, i: Int) {
                    ps.setString(1, records[i].sykepengesoknadId)
                    ps.setString(2, records[i].status.toString())
                    ps.setTimestamp(3, Timestamp.from(records[i].opprettet))
                    ps.setTimestamp(4, Timestamp.from(records[i].modifisert))
                    ps.setTimestamp(
                        5,
                        if (records[i].timeout == null) {
                            null
                        } else {
                            Timestamp.from(records[i].timeout)
                        }
                    )
                    if (records[i].avstemt != null) {

                        ps.setBoolean(6, records[i].avstemt!!)
                    } else {
                        ps.setNull(6, java.sql.Types.BOOLEAN)
                    }
                }

                override fun getBatchSize() = records.size
            }
        )
    }
}

package no.nav.syfo.oppgave

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.consumer.oppgave.OppgaveRequest
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
@Repository
class UtsattOppgaveDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    companion object {
        private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    fun lagreUtsattOppgave(innsendingUuid: String, oppgaveRequest: OppgaveRequest) = namedParameterJdbcTemplate.update(
        """
            INSERT INTO UTSATTE_OPPGAVER(id, innsending_uuid, oppgave_foresporsel) 
            VALUES(:id, :innsending_uuid, :oppgave_foresporsel)
            """,
        mutableMapOf(
            "id" to UUID.randomUUID(),
            "innsending_uuid" to innsendingUuid,
            "oppgave_foresporsel" to objectMapper.writeValueAsBytes(oppgaveRequest)
        )
    )

    fun hentUtsattOppgaverForAktorId(aktorId: String): List<UtsattOppgave> = namedParameterJdbcTemplate.query(
        """
            SELECT * 
            FROM UTSATTE_OPPGAVER u
            INNER JOIN INNSENDING i ON u.innsending_uuid = i.innsending_uuid 
            WHERE aktor_id = :aktor_id
        """,
        mutableMapOf("aktor_id" to aktorId)
    ) { resultSet, _ ->
        UtsattOppgave(
            innsendingId = resultSet.getString("innsending_uuid"),
            oppgaveRequest = resultSet.getBlob("oppgave_request").binaryStream.use {
                objectMapper.readValue<OppgaveRequest>(it)
            }
        )
    }

    fun fjernUtsattOppgave(innsendingUuid: String) = namedParameterJdbcTemplate.update(
        "DELETE FROM UTSATTE_OPPGAVER WHERE innsending_uuid = :innsending_uuid",
        mutableMapOf("innsending_uuid" to innsendingUuid)
    )
}

data class UtsattOppgave(
    val innsendingId: String,
    val oppgaveRequest: OppgaveRequest
)

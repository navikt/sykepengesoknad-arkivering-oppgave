package no.nav.helse.flex.e2e

import com.nhaarman.mockitokotlin2.whenever
import no.nav.helse.FellesTestoppsett
import no.nav.helse.flex.any
import no.nav.helse.flex.client.SyfosoknadClient
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.kafka.consumer.AivenSoknadSendtListener
import no.nav.helse.flex.kafka.consumer.AivenSpreOppgaverListener
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.InnsendingDbRecord
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.BehandleVedTimeoutService
import no.nav.helse.flex.service.SaksbehandlingsService
import no.nav.helse.flex.skapConsumerRecord
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvartypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.annotation.DirtiesContext
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@DirtiesContext
class BigQueryTest : FellesTestoppsett() {

    @MockBean
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @MockBean
    lateinit var acknowledgment: Acknowledgment

    @MockBean
    lateinit var syfosoknadClient: SyfosoknadClient

    @Autowired
    lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @Autowired
    lateinit var behandleVedTimeoutService: BehandleVedTimeoutService

    @Autowired
    lateinit var aivenSoknadSendtListener: AivenSoknadSendtListener

    @Autowired
    lateinit var aivenSpreOppgaverListener: AivenSpreOppgaverListener

    @Autowired
    lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setup() {
        whenever(saksbehandlingsService.finnEksisterendeInnsending(any())).thenAnswer {
            InnsendingDbRecord(
                id = "iid",
                sykepengesoknadId = it.arguments[0].toString(),
                journalpostId = "journalpost"
            )
        }
        whenever(syfosoknadClient.hentSoknad(any())).thenReturn(
            objectMapper.readValue(
                lagSoknad().serialisertTilString(),
                SykepengesoknadDTO::class.java
            )
        )
    }

    @Test
    fun `test spørring brukt i BigQuery Federated Query`() {
        val id1 = UUID.randomUUID()
        leggOppgavePaAivenKafka(
            OppgaveDTO(
                DokumentTypeDTO.Søknad,
                OppdateringstypeDTO.Opprett,
                id1,
                E2ETest.omFireTimer
            )
        )
        leggSoknadPaKafka(lagSoknad(id1))

        val id2 = UUID.randomUUID()
        leggOppgavePaAivenKafka(
            OppgaveDTO(
                DokumentTypeDTO.Søknad,
                OppdateringstypeDTO.OpprettSpeilRelatert,
                id2,
                E2ETest.omFireTimer
            )
        )
        leggSoknadPaKafka(lagSoknad(id2))

        val id3 = UUID.randomUUID()
        leggOppgavePaAivenKafka(
            OppgaveDTO(
                DokumentTypeDTO.Søknad,
                OppdateringstypeDTO.Utsett,
                id3,
                LocalDateTime.now().minusHours(4)
            )
        )
        leggSoknadPaKafka(lagSoknad(id3))

        val id4 = UUID.randomUUID()
        leggOppgavePaAivenKafka(
            OppgaveDTO(
                DokumentTypeDTO.Søknad,
                OppdateringstypeDTO.Opprett,
                id4,
                E2ETest.omFireTimer
            )
        )

        val id5 = UUID.randomUUID()
        leggOppgavePaAivenKafka(
            OppgaveDTO(
                DokumentTypeDTO.Søknad,
                OppdateringstypeDTO.Utsett,
                id5,
                E2ETest.omFireTimer
            )
        )
        leggSoknadPaKafka(lagSoknad(id5))

        val id6 = UUID.randomUUID()
        leggOppgavePaAivenKafka(
            OppgaveDTO(
                DokumentTypeDTO.Søknad,
                OppdateringstypeDTO.Ferdigbehandlet,
                id6,
                null
            )
        )
        leggSoknadPaKafka(lagSoknad(id6))

        behandleVedTimeoutService.behandleTimeout()

        val params = MapSqlParameterSource().addValue(
            "status",
            listOf("Opprettet", "OpprettetSpeilRelatert", "OpprettetTimeout")
        )

        val sql =
            """
            SELECT sykepengesoknad_id, status, modifisert 
            FROM oppgavestyring
            WHERE status IN (:status)
            ORDER BY modifisert
            """
        val tilBigQuery = namedParameterJdbcTemplate.query(sql, params) { rs: ResultSet, _ -> rs.toBigQueryRecord() }

        tilBigQuery.size `should be` 3
    }

    private fun leggSoknadPaKafka(soknad: SykepengesoknadDTO) =
        aivenSoknadSendtListener.listen(skapConsumerRecord("key", soknad.serialisertTilString()), acknowledgment)

    private fun leggOppgavePaAivenKafka(oppgave: OppgaveDTO) =
        aivenSpreOppgaverListener.listen(skapConsumerRecord("key", oppgave.serialisertTilString()), acknowledgment)

    private fun lagSoknad(
        soknadId: UUID = UUID.randomUUID(),
        sendtNav: LocalDateTime? = LocalDateTime.now(),
        sendtArbeidsgiver: LocalDateTime? = null
    ) = SykepengesoknadDTO(
        fnr = E2ETest.fnr,
        id = soknadId.toString(),
        opprettet = LocalDateTime.now(),
        fom = LocalDate.of(2019, 5, 4),
        tom = LocalDate.of(2019, 5, 8),
        type = SoknadstypeDTO.ARBEIDSTAKERE,
        sporsmal = listOf(
            SporsmalDTO(
                id = UUID.randomUUID().toString(),
                tag = "TAGGEN",
                sporsmalstekst = "Har systemet gode integrasjonstester?",
                svartype = SvartypeDTO.JA_NEI,
                svar = listOf(SvarDTO(verdi = "JA"))

            )
        ),
        status = SoknadsstatusDTO.SENDT,
        sendtNav = sendtNav,
        sendtArbeidsgiver = sendtArbeidsgiver,
    )

    private fun ResultSet.toBigQueryRecord(): BigQueryRecord {
        return BigQueryRecord(
            sykepengesoknadId = getString("sykepengesoknad_id"),
            status = OppgaveStatus.valueOf(getString("status")),
            modifisert = getTimestamp("modifisert").toInstant(),
        )
    }

    private data class BigQueryRecord(
        val sykepengesoknadId: String,
        val status: OppgaveStatus,
        val modifisert: Instant
    )
}

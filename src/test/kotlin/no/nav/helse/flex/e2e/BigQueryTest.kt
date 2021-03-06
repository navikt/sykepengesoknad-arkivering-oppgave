package no.nav.helse.flex.e2e

import com.nhaarman.mockitokotlin2.whenever
import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.any
import no.nav.helse.flex.client.SykepengesoknadBackendClient
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.InnsendingDbRecord
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveDbRecord
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.OppgaveOpprettelse
import no.nav.helse.flex.service.SaksbehandlingsService
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvartypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.test.annotation.DirtiesContext
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@DirtiesContext
class BigQueryTest : FellesTestoppsett() {

    @MockBean
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @MockBean
    lateinit var sykepengesoknadBackendClient: SykepengesoknadBackendClient

    @Autowired
    lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @Autowired
    lateinit var oppgaveOpprettelse: OppgaveOpprettelse

    private val tid = LocalDateTime.of(2022, 3, 7, 12, 37, 17).toInstant(ZoneOffset.UTC)

    @BeforeEach
    fun `lag oppgaver`() {
        val enDagSiden = tid.minus(1, ChronoUnit.DAYS)
        val omEnDag = tid.plus(1, ChronoUnit.DAYS)
        val enTimeSiden = tid.minus(1, ChronoUnit.HOURS)

        // B??mlo har sagt det skal opprettes en oppgave.
        spreOppgaveRepository.save(
            SpreOppgaveDbRecord(
                sykepengesoknadId = "uuid-1",
                timeout = null,
                status = OppgaveStatus.Opprett,
                avstemt = true,
                opprettet = enDagSiden,
                modifisert = enDagSiden
            )
        )

        // B??mlo har sagt det skal opprettes en Speil-relatert oppgave.
        spreOppgaveRepository.save(
            SpreOppgaveDbRecord(
                sykepengesoknadId = "uuid-2",
                timeout = null,
                status = OppgaveStatus.OpprettSpeilRelatert,
                avstemt = true,
                opprettet = enDagSiden,
                modifisert = enDagSiden
            )
        )

        // Utsett har timet ut, s?? det skal opprettes en oppgave.
        spreOppgaveRepository.save(
            SpreOppgaveDbRecord(
                sykepengesoknadId = "uuid-3",
                timeout = enTimeSiden,
                status = OppgaveStatus.Utsett,
                avstemt = true,
                opprettet = enDagSiden,
                modifisert = enDagSiden
            )
        )

        // Er ikke avstemt s?? det skal ikke opprettes en oppgave.
        spreOppgaveRepository.save(
            SpreOppgaveDbRecord(
                sykepengesoknadId = "uuid-4",
                timeout = null,
                status = OppgaveStatus.Opprett,
                avstemt = false,
                opprettet = enDagSiden,
                modifisert = enDagSiden
            )
        )

        // Utsett ikke n??dd timeout s?? det skal ikke opprettes en oppgave.
        spreOppgaveRepository.save(
            SpreOppgaveDbRecord(
                sykepengesoknadId = "uuid-5",
                timeout = omEnDag,
                status = OppgaveStatus.Utsett,
                avstemt = true,
                opprettet = enDagSiden,
                modifisert = enDagSiden
            )
        )

        // Er ferdigbehandlet s?? det skal ikke opprettes en oppgave.
        spreOppgaveRepository.save(
            SpreOppgaveDbRecord(
                sykepengesoknadId = "uuid-6",
                timeout = null,
                status = OppgaveStatus.IkkeOpprett,
                avstemt = true,
                opprettet = enDagSiden,
                modifisert = enDagSiden
            )
        )
        oppgaveOpprettelse.behandleOppgaver(tid)
    }

    @BeforeEach
    fun setup() {
        whenever(saksbehandlingsService.finnEksisterendeInnsending(any())).thenAnswer {
            InnsendingDbRecord(
                id = "id",
                sykepengesoknadId = it.arguments[0].toString(),
                journalpostId = "journalpost"
            )
        }
        whenever(sykepengesoknadBackendClient.hentSoknad(any())).thenReturn(
            objectMapper.readValue(
                lagSoknad().serialisertTilString(),
                SykepengesoknadDTO::class.java
            )
        )
    }

    @AfterEach
    fun `slett oppgaver`() {
        jdbcTemplate.update("DELETE FROM oppgavestyring")
    }

    @Test
    fun `test Federated Query for oppgaver opprettet`() {
        val sql =
            """
            SELECT sykepengesoknad_id, status, modifisert AS opprettet
            FROM oppgavestyring
            WHERE modifisert >= :modifisert
              AND status IN (:status)
            ORDER BY modifisert
            """

        val params = MapSqlParameterSource()
            .addValue("status", listOf("Opprettet", "OpprettetSpeilRelatert", "OpprettetTimeout"))
            .addValue("modifisert", Timestamp.from(tid))

        val tilBigQuery = namedParameterJdbcTemplate.query(sql, params) { rs: ResultSet, _ -> rs.toBigQueryOpprettet() }

        tilBigQuery.size `should be` 3

        val uuids = tilBigQuery.map { it.sykepengesoknadId }.toSet()

        uuids.containsAll(listOf("uuid-1", "uuid-2", "uuid-3")) `should be` true
    }

    @Test
    fun `test Federated Query for oppgaver opprettet gruppert`() {
        val sql =
            """
            SELECT date(modifisert) AS dato, status, count(*) AS antall
            FROM oppgavestyring
            WHERE modifisert >= :modifisert
              AND status IN (:status)
            GROUP BY date(modifisert), status;
            """

        val params = MapSqlParameterSource()
            .addValue("status", listOf("Opprettet", "OpprettetSpeilRelatert", "OpprettetTimeout"))
            .addValue("modifisert", Timestamp.from(tid))

        val tilBigQuery = namedParameterJdbcTemplate.query(sql, params) { rs: ResultSet, _ -> rs.toBigQueryGruppert() }

        tilBigQuery.size `should be` 3

        tilBigQuery.first { it.status == OppgaveStatus.Opprettet }.antall `should be` 1
        tilBigQuery.first { it.status == OppgaveStatus.OpprettetSpeilRelatert }.antall `should be` 1
        tilBigQuery.first { it.status == OppgaveStatus.OpprettetTimeout }.antall `should be` 1
    }

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

    private fun ResultSet.toBigQueryOpprettet(): BigQueryOpprettet {
        return BigQueryOpprettet(
            sykepengesoknadId = getString("sykepengesoknad_id"),
            status = OppgaveStatus.valueOf(getString("status")),
            opprettet = getTimestamp("opprettet").toInstant(),
        )
    }

    private fun ResultSet.toBigQueryGruppert(): BigQueryGruppert {
        return BigQueryGruppert(
            dato = getDate("dato").toLocalDate(),
            status = OppgaveStatus.valueOf(getString("status")),
            antall = getInt("antall"),
        )
    }

    private data class BigQueryOpprettet(
        val sykepengesoknadId: String,
        val status: OppgaveStatus,
        val opprettet: Instant
    )

    private data class BigQueryGruppert(
        val dato: LocalDate,
        val status: OppgaveStatus,
        val antall: Int
    )
}

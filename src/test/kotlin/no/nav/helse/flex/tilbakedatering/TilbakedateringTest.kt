package no.nav.helse.flex.tilbakedatering

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.*
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.mockdispatcher.OppgaveMockDispatcher
import no.nav.helse.flex.service.*
import no.nav.helse.flex.sykepengesoknad.kafka.MerknadDTO
import no.nav.helse.flex.tilbakedaterte.OppgaverForTilbakedaterteStatus
import no.nav.syfo.model.Merknad
import okhttp3.mockwebserver.MockResponse
import org.amshove.kluent.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.annotation.DirtiesContext
import søknad
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit

@DirtiesContext
class TilbakedateringTest : FellesTestOppsett() {
    val fnr = "fnr"

    @BeforeEach
    fun setup() {
        OppgaveMockDispatcher.reset()
        oppgaverForTilbakedaterteRepository.deleteAll()
    }

    @Test
    fun `En søknad under behandling for tilbakedatering får behandlingstype ae0239 og patches når sykmeldinga er behandlet`() {
        OppgaveMockDispatcher.getOppdaterOppgaveRequest().shouldBeEmpty()

        oppgaverForTilbakedaterteRepository.findAll().toList().shouldBeEmpty()
        val soknadId = UUID.randomUUID()
        val søknad = søknad(soknadId).copy(merknaderFraSykmelding = listOf(MerknadDTO("UNDER_BEHANDLING", "bla bla")))

        sykepengesoknadMockWebserver.enqueue(
            MockResponse().setBody(søknad.serialisertTilString()).addHeader("Content-Type", "application/json"),
        )
        leggSøknadPåKafka(søknad)
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))

        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.behandlingstype).isEqualTo("ae0239")

        val sykepengesoknadRequest = sykepengesoknadMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(sykepengesoknadRequest.requestLine).isEqualTo("GET /api/v3/soknader/${søknad.id}/kafkaformat HTTP/1.1")

        val tilbakedaterte = oppgaverForTilbakedaterteRepository.findAll().toList()
        tilbakedaterte.shouldHaveSize(1)
        tilbakedaterte.first().sykepengesoknadUuid `should be equal to` soknadId.toString()
        tilbakedaterte.first().sykmeldingUuid `should be equal to` søknad.sykmeldingId
        tilbakedaterte.first().status `should be equal to` OppgaverForTilbakedaterteStatus.OPPRETTET

        // Sykmeldinga blir godkjent og vi endrer behandlingstema
        leggSykmeldingPåKafka(sykmeldingKafkaMessage(fnr = fnr, sykmeldingId = søknad.sykmeldingId!!))

        val lastOppdaterOppgaveReqeust = OppgaveMockDispatcher.getLastOppdaterOppgaveReqeust()
        lastOppdaterOppgaveReqeust.behandlingstype.shouldBeNull()
        lastOppdaterOppgaveReqeust.behandlingstema `should be equal to` "ab0061"
        oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS) // Hent
        oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS) // Patch

        val oppgave = oppgaverForTilbakedaterteRepository.findBySykmeldingUuid(søknad.sykmeldingId!!)
        oppgave.shouldHaveSize(1)
        oppgave.first().status `should be equal to` OppgaverForTilbakedaterteStatus.OPPDATERT
        oppgave.first().oppdatert.shouldNotBeNull()

        sykepengesoknadMockWebserver.takeRequest(5, TimeUnit.SECONDS) // søknaden hentes for å beregne tema
    }

    @Test
    fun `En søknad under behandling for tilbakedatering får behandlingstype og patches ikke når oppgaven er ferdigstilt`() {
        OppgaveMockDispatcher.getOppdaterOppgaveRequest().shouldBeEmpty()

        oppgaverForTilbakedaterteRepository.findAll().toList().shouldBeEmpty()
        val soknadId = UUID.randomUUID()
        val søknad = søknad(soknadId).copy(merknaderFraSykmelding = listOf(MerknadDTO("UNDER_BEHANDLING", "bla bla")))

        sykepengesoknadMockWebserver.enqueue(
            MockResponse().setBody(søknad.serialisertTilString()).addHeader("Content-Type", "application/json"),
        )
        leggSøknadPåKafka(søknad)
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))

        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.behandlingstype).isEqualTo("ae0239")

        val sykepengesoknadRequest = sykepengesoknadMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(sykepengesoknadRequest.requestLine).isEqualTo("GET /api/v3/soknader/${søknad.id}/kafkaformat HTTP/1.1")

        val tilbakedaterte = oppgaverForTilbakedaterteRepository.findAll().toList()
        tilbakedaterte.shouldHaveSize(1)
        tilbakedaterte.first().sykepengesoknadUuid `should be equal to` soknadId.toString()
        tilbakedaterte.first().sykmeldingUuid `should be equal to` søknad.sykmeldingId
        tilbakedaterte.first().status `should be equal to` OppgaverForTilbakedaterteStatus.OPPRETTET

        // Sykmeldinga blir godkjent og vi endrer ikke behandlignstema fordi oppgaven er ferdigstilt

        OppgaveMockDispatcher.enqueueResponse(
            MockResponse().setBody(
                HentOppgaveResponse("FERDIGSTILT").serialisertTilString(),
            ),
        )

        leggSykmeldingPåKafka(sykmeldingKafkaMessage(fnr = fnr, sykmeldingId = søknad.sykmeldingId!!))

        val oppgave = oppgaverForTilbakedaterteRepository.findBySykmeldingUuid(søknad.sykmeldingId!!)
        oppgave.shouldHaveSize(1)
        oppgave.first().status `should be equal to` OppgaverForTilbakedaterteStatus.OPPGAVE_ALLEREDE_FERDIGSTILT
        oppgave.first().oppdatert.shouldNotBeNull()
        oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS) // Hent
        OppgaveMockDispatcher.getOppdaterOppgaveRequest().shouldBeEmpty()
    }

    @Test
    fun `En søknad under behandling for tilbakedatering får behandlingstype ae0239 og patches ikke når sykmeldinga ikke er godkjent`() {
        OppgaveMockDispatcher.getOppdaterOppgaveRequest().shouldBeEmpty()

        oppgaverForTilbakedaterteRepository.findAll().toList().shouldBeEmpty()
        val soknadId = UUID.randomUUID()
        val søknad = søknad(soknadId).copy(merknaderFraSykmelding = listOf(MerknadDTO("UNDER_BEHANDLING", "bla bla")))

        sykepengesoknadMockWebserver.enqueue(
            MockResponse().setBody(søknad.serialisertTilString()).addHeader("Content-Type", "application/json"),
        )
        leggSøknadPåKafka(søknad)
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))

        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.behandlingstype).isEqualTo("ae0239")

        val sykepengesoknadRequest = sykepengesoknadMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(sykepengesoknadRequest.requestLine).isEqualTo("GET /api/v3/soknader/${søknad.id}/kafkaformat HTTP/1.1")

        val tilbakedaterte = oppgaverForTilbakedaterteRepository.findAll().toList()
        tilbakedaterte.shouldHaveSize(1)
        tilbakedaterte.first().sykepengesoknadUuid `should be equal to` soknadId.toString()
        tilbakedaterte.first().sykmeldingUuid `should be equal to` søknad.sykmeldingId
        tilbakedaterte.first().status `should be equal to` OppgaverForTilbakedaterteStatus.OPPRETTET

        // Sykmeldinga blir godkjent og vi endrer behandlingstema
        leggSykmeldingPåKafka(
            sykmeldingKafkaMessage(
                fnr = fnr,
                sykmeldingId = søknad.sykmeldingId!!,
                merknader =
                    listOf(
                        Merknad("TILBAKEDATERING_IKKE_GODKJENT", "bla bla"),
                    ),
            ),
        )

        val oppgave = oppgaverForTilbakedaterteRepository.findBySykmeldingUuid(søknad.sykmeldingId!!)
        oppgave.shouldHaveSize(1)
        oppgave.first().status `should be equal to` OppgaverForTilbakedaterteStatus.IKKE_GODKJENT
        oppgave.first().oppdatert.shouldNotBeNull()
    }
}

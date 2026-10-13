package no.nav.helse.flex.tilbakedatering

import lagSoknad
import no.nav.helse.flex.FellesTestOppsett
import no.nav.helse.flex.bodyAsString
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.jsonResponse
import no.nav.helse.flex.mockdispatcher.OppgaveMockDispatcher
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.HentOppgaveResponse
import no.nav.helse.flex.service.OppgaveRequest
import no.nav.helse.flex.sykepengesoknad.kafka.MerknadDTO
import no.nav.helse.flex.tilbakedaterte.OppgaverForTilbakedaterteStatus
import no.nav.syfo.model.Merknad
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldNotBeNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.annotation.DirtiesContext
import tools.jackson.module.kotlin.readValue
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
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
        val søknad = lagSoknad(soknadId).copy(merknaderFraSykmelding = listOf(MerknadDTO("UNDER_BEHANDLING", "bla bla")))

        sykepengesoknadMockWebserver.enqueue(
            jsonResponse(søknad.serialisertTilString()),
        )
        leggSoknadPaaKafka(søknad)
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))

        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.bodyAsString())
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
        val søknad = lagSoknad(soknadId).copy(merknaderFraSykmelding = listOf(MerknadDTO("UNDER_BEHANDLING", "bla bla")))

        sykepengesoknadMockWebserver.enqueue(
            jsonResponse(søknad.serialisertTilString()),
        )
        leggSoknadPaaKafka(søknad)
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))

        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.bodyAsString())
        assertThat(oppgaveRequestBody.behandlingstype).isEqualTo("ae0239")

        val sykepengesoknadRequest = sykepengesoknadMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(sykepengesoknadRequest.requestLine).isEqualTo("GET /api/v3/soknader/${søknad.id}/kafkaformat HTTP/1.1")

        val tilbakedaterte = oppgaverForTilbakedaterteRepository.findAll().toList()
        tilbakedaterte.shouldHaveSize(1)
        tilbakedaterte.first().sykepengesoknadUuid `should be equal to` soknadId.toString()
        tilbakedaterte.first().sykmeldingUuid `should be equal to` søknad.sykmeldingId
        tilbakedaterte.first().status `should be equal to` OppgaverForTilbakedaterteStatus.OPPRETTET

        // Sykmeldinga blir godkjent og vi endrer ikke behandlignstema fordi oppgaven er ferdigstilt

        OppgaveMockDispatcher.enqueue(
            jsonResponse(
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
        val soknad = lagSoknad(soknadId).copy(merknaderFraSykmelding = listOf(MerknadDTO("UNDER_BEHANDLING", "bla bla")))

        sykepengesoknadMockWebserver.enqueue(
            jsonResponse(soknad.serialisertTilString()),
        )
        leggSoknadPaaKafka(soknad)
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))

        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.bodyAsString())
        assertThat(oppgaveRequestBody.behandlingstype).isEqualTo("ae0239")

        val sykepengesoknadRequest = sykepengesoknadMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(sykepengesoknadRequest.requestLine).isEqualTo("GET /api/v3/soknader/${soknad.id}/kafkaformat HTTP/1.1")

        val tilbakedaterte = oppgaverForTilbakedaterteRepository.findAll().toList()
        tilbakedaterte.shouldHaveSize(1)
        tilbakedaterte.first().sykepengesoknadUuid `should be equal to` soknadId.toString()
        tilbakedaterte.first().sykmeldingUuid `should be equal to` soknad.sykmeldingId
        tilbakedaterte.first().status `should be equal to` OppgaverForTilbakedaterteStatus.OPPRETTET

        // Sykmeldinga blir godkjent og vi endrer behandlingstema
        leggSykmeldingPåKafka(
            sykmeldingKafkaMessage(
                fnr = fnr,
                sykmeldingId = soknad.sykmeldingId!!,
                merknader =
                    listOf(
                        Merknad("TILBAKEDATERING_IKKE_GODKJENT", "bla bla"),
                    ),
            ),
        )

        val oppgave = oppgaverForTilbakedaterteRepository.findBySykmeldingUuid(soknad.sykmeldingId!!)
        oppgave.shouldHaveSize(1)
        oppgave.first().status `should be equal to` OppgaverForTilbakedaterteStatus.IKKE_GODKJENT
        oppgave.first().oppdatert.shouldNotBeNull()
    }
}

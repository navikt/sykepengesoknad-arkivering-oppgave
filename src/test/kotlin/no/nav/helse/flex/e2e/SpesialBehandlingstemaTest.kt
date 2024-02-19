package no.nav.helse.flex.e2e

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.*
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.service.*
import okhttp3.mockwebserver.MockResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.annotation.DirtiesContext
import søknad
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit

@DirtiesContext
class SpesialBehandlingstemaTest : FellesTestOppsett() {
    val fnr = "fnr"

    @Test
    fun `En speil relatert søknad får behandlingstema ab0455`() {
        val soknadId = UUID.randomUUID()
        val søknad = søknad(soknadId)

        sykepengesoknadMockWebserver.enqueue(
            MockResponse().setBody(søknad.serialisertTilString()).addHeader("Content-Type", "application/json"),
        )
        leggSøknadPåKafka(søknad)
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.OpprettSpeilRelatert, soknadId))

        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.behandlingstema).isEqualTo("ab0455")

        val sykepengesoknadRequest = sykepengesoknadMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(sykepengesoknadRequest.requestLine).isEqualTo("GET /api/v3/soknader/${søknad.id}/kafkaformat HTTP/1.1")
    }

    @Test
    fun `En ikke speil relatert søknad får behandlingstema ab0061`() {
        val soknadId = UUID.randomUUID()
        val søknad = søknad(soknadId)

        sykepengesoknadMockWebserver.enqueue(
            MockResponse().setBody(søknad.serialisertTilString()).addHeader("Content-Type", "application/json"),
        )
        leggSøknadPåKafka(søknad)
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))

        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.behandlingstema).isEqualTo("ab0061")

        val sykepengesoknadRequest = sykepengesoknadMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(sykepengesoknadRequest.requestLine).isEqualTo("GET /api/v3/soknader/${søknad.id}/kafkaformat HTTP/1.1")
    }

    @Test
    fun `En søknad tilhørende utenlandsk sykmelding får behandlingstype ae0106`() {
        val soknadId = UUID.randomUUID()
        val søknad = søknad(soknadId, utenlandskSykmelding = true)

        sykepengesoknadMockWebserver.enqueue(
            MockResponse().setBody(søknad.serialisertTilString()).addHeader("Content-Type", "application/json"),
        )
        leggSøknadPåKafka(søknad)
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))

        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.behandlingstype).isEqualTo("ae0106")

        val sykepengesoknadRequest = sykepengesoknadMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(sykepengesoknadRequest.requestLine).isEqualTo("GET /api/v3/soknader/${søknad.id}/kafkaformat HTTP/1.1")
    }
}

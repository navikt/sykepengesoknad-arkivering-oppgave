package no.nav.helse.flex.e2e

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.*
import no.nav.helse.flex.service.*
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import okhttp3.mockwebserver.MockResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import søknad
import java.util.UUID
import java.util.concurrent.TimeUnit

@DirtiesContext
class ArbeidsledigIntegrationTest : FellesTestoppsett() {

    @Autowired
    lateinit var oppgaveOpprettelse: OppgaveOpprettelse

    @Test
    fun `En arbeidsledigsøknad får behandlingstema ab0426`() {
        val soknadId = UUID.randomUUID()
        val søknad = søknad(soknadId).copy(type = SoknadstypeDTO.ARBEIDSLEDIG)

        sykepengesoknadMockWebserver.enqueue(
            MockResponse().setBody(søknad.serialisertTilString()).addHeader("Content-Type", "application/json")
        )
        leggSøknadPåKafka(søknad)

        oppgaveOpprettelse.behandleOppgaver()

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.behandlingstema).isEqualTo("ab0426")
    }
}

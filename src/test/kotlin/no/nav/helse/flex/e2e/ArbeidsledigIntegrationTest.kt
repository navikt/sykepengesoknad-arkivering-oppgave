package no.nav.helse.flex.e2e

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.*
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.mockdispatcher.SykepengesoknadMockDispatcher
import no.nav.helse.flex.service.*
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import org.amshove.kluent.`should be null`
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.annotation.DirtiesContext
import søknad
import java.util.UUID
import java.util.concurrent.TimeUnit

@DirtiesContext
class ArbeidsledigIntegrationTest : FellesTestoppsett() {

    @Test
    fun `En arbeidsledigsøknad får behandlingstema ab0426 og takler at bømlo sier opprett`() {
        val soknadId = UUID.randomUUID()
        val søknad = søknad(soknadId).copy(type = SoknadstypeDTO.ARBEIDSLEDIG)

        SykepengesoknadMockDispatcher.enque(søknad)

        leggSøknadPåKafka(søknad)
        oppgaveOpprettelse.behandleOppgaver()

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(2, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.behandlingstema).isEqualTo("ab0426")

        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))
        oppgaveOpprettelse.behandleOppgaver()
        oppgaveMockWebserver.takeRequest(1, TimeUnit.SECONDS).`should be null`()
    }
}

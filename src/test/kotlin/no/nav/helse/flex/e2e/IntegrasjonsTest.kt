package no.nav.helse.flex.e2e

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.*
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.mockdispatcher.SykepengesoknadMockDispatcher
import no.nav.helse.flex.service.*
import no.nav.helse.flex.sykepengesoknad.kafka.ArbeidssituasjonDTO
import no.nav.helse.flex.sykepengesoknad.kafka.FiskerBladDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import org.amshove.kluent.`should be null`
import org.amshove.kluent.shouldBeEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.annotation.DirtiesContext
import søknad
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit

@DirtiesContext
class IntegrasjonsTest : FellesTestoppsett() {
    @Test
    fun `En arbeidsledigsøknad får behandlingstema ab0426 og takler at bømlo sier opprett`() {
        val soknadId = UUID.randomUUID()
        val søknad = søknad(soknadId).copy(type = SoknadstypeDTO.ARBEIDSLEDIG, arbeidssituasjon = ArbeidssituasjonDTO.ARBEIDSLEDIG)

        SykepengesoknadMockDispatcher.enque(søknad)

        leggSøknadPåKafka(søknad)
        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(49, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(2, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.behandlingstema).isEqualTo("ab0426")

        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))
        oppgaveOpprettelse.behandleOppgaver()
        oppgaveMockWebserver.takeRequest(1, TimeUnit.SECONDS).`should be null`()

        val pdfRequest = pdfMockWebserver.takeRequest(10, TimeUnit.SECONDS)!!
        val pdfRequestBody = objectMapper.readValue<JsonNode>(pdfRequest.body.readUtf8())
        pdfRequestBody.get("arbeidssituasjonTekst").textValue() shouldBeEqualTo "arbeidsledig"
    }

    @Test
    fun `En fiskersøknad med fiskerblad i oppgavebeskrivelsen`() {
        val soknadId = UUID.randomUUID()
        val søknad =
            søknad(soknadId).copy(
                type = SoknadstypeDTO.SELVSTENDIGE_OG_FRILANSERE,
                arbeidssituasjon = ArbeidssituasjonDTO.FISKER,
                fiskerBlad = FiskerBladDTO.A,
            )

        SykepengesoknadMockDispatcher.enque(søknad)

        leggSøknadPåKafka(søknad)
        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(49, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(2, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.behandlingstema).isEqualTo("ab0061")
        assertThat(oppgaveRequestBody.beskrivelse).isEqualTo(
            """
            Søknad om sykepenger for fisker på blad A for perioden 04.05.2019 til 08.05.2019
            
            Har systemet gode integrasjonstester?
            Ja
            """.trimIndent(),
        )

        val pdfRequest = pdfMockWebserver.takeRequest(10, TimeUnit.SECONDS)!!
        val pdfRequestBody = objectMapper.readValue<JsonNode>(pdfRequest.body.readUtf8())
        pdfRequestBody.get("arbeidssituasjonTekst").textValue() shouldBeEqualTo "fisker"
    }
}

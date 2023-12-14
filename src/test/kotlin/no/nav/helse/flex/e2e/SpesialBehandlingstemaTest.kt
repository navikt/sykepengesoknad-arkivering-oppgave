package no.nav.helse.flex.e2e

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.*
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.kafka.consumer.AivenSoknadSendtListener
import no.nav.helse.flex.kafka.consumer.AivenSpreOppgaverListener
import no.nav.helse.flex.service.*
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvartypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import okhttp3.mockwebserver.MockResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

@DirtiesContext
class SpesialBehandlingstemaTest : FellesTestoppsett() {
    @MockBean
    lateinit var acknowledgment: Acknowledgment

    @Autowired
    lateinit var aivenSoknadSendtListener: AivenSoknadSendtListener

    @Autowired
    lateinit var aivenSpreOppgaverListener: AivenSpreOppgaverListener

    @Autowired
    lateinit var oppgaveOpprettelse: OppgaveOpprettelse

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

        oppgaveOpprettelse.behandleOppgaver()

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

        oppgaveOpprettelse.behandleOppgaver()

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

        oppgaveOpprettelse.behandleOppgaver()

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.behandlingstype).isEqualTo("ae0106")

        val sykepengesoknadRequest = sykepengesoknadMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(sykepengesoknadRequest.requestLine).isEqualTo("GET /api/v3/soknader/${søknad.id}/kafkaformat HTTP/1.1")
    }

    private fun leggSøknadPåKafka(søknad: SykepengesoknadDTO) =
        aivenSoknadSendtListener.listen(skapConsumerRecord("key", søknad.serialisertTilString()), acknowledgment)

    private fun leggOppgavePåAivenKafka(oppgave: OppgaveDTO) =
        aivenSpreOppgaverListener.listen(skapConsumerRecord("key", oppgave.serialisertTilString()), acknowledgment)

    private fun søknad(
        soknadId: UUID = UUID.randomUUID(),
        sendtNav: LocalDateTime? = LocalDateTime.now(),
        sendtArbeidsgiver: LocalDateTime? = null,
        utenlandskSykmelding: Boolean? = null,
    ) = SykepengesoknadDTO(
        fnr = fnr,
        id = soknadId.toString(),
        opprettet = LocalDateTime.now(),
        fom = LocalDate.of(2019, 5, 4),
        tom = LocalDate.of(2019, 5, 8),
        type = SoknadstypeDTO.ARBEIDSTAKERE,
        sporsmal =
            listOf(
                SporsmalDTO(
                    id = UUID.randomUUID().toString(),
                    tag = "TAGGEN",
                    sporsmalstekst = "Har systemet gode integrasjonstester?",
                    svartype = SvartypeDTO.JA_NEI,
                    svar = listOf(SvarDTO(verdi = "JA")),
                ),
            ),
        status = SoknadsstatusDTO.SENDT,
        sendtNav = sendtNav,
        sendtArbeidsgiver = sendtArbeidsgiver,
        utenlandskSykmelding = utenlandskSykmelding,
    )
}

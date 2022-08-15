package no.nav.helse.flex.e2e

import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.any
import no.nav.helse.flex.arkivering.Arkivaren
import no.nav.helse.flex.client.SykepengesoknadBackendClient
import no.nav.helse.flex.client.pdl.PdlClient
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.kafka.consumer.AivenSoknadSendtListener
import no.nav.helse.flex.kafka.consumer.AivenSpreOppgaverListener
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.*
import no.nav.helse.flex.skapConsumerRecord
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvartypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@DirtiesContext
class OpprettSpeilrelatertTest : FellesTestoppsett() {

    companion object {
        const val aktorId = "aktørId"
        const val fnr = "fnr"
    }

    @MockBean
    lateinit var oppgaveService: OppgaveService

    @MockBean
    lateinit var arkivaren: Arkivaren

    @MockBean
    lateinit var identService: IdentService

    @MockBean
    lateinit var pdlClient: PdlClient

    @MockBean
    lateinit var acknowledgment: Acknowledgment

    @MockBean
    lateinit var sykepengesoknadBackendClient: SykepengesoknadBackendClient

    @Autowired
    lateinit var aivenSoknadSendtListener: AivenSoknadSendtListener

    @Autowired
    lateinit var aivenSpreOppgaverListener: AivenSpreOppgaverListener

    @Autowired
    lateinit var oppgaveOpprettelse: OppgaveOpprettelse

    @BeforeEach
    fun setup() {
        whenever(identService.hentAktorIdForFnr(any())).thenReturn(aktorId)
        whenever(identService.hentFnrForAktorId(any())).thenReturn(fnr)
        whenever(pdlClient.hentFormattertNavn(any())).thenReturn("Kalle Klovn")
        whenever(arkivaren.opprettJournalpost(any())).thenReturn("jpost1234")
        whenever(oppgaveService.opprettOppgave(any())).thenReturn(OppgaveResponse(123, "4488", "SYK", "SOK"))
        whenever(sykepengesoknadBackendClient.hentSoknad(any())).thenReturn(
            objectMapper.readValue(
                søknad().serialisertTilString(),
                SykepengesoknadDTO::class.java
            )
        )
    }

    @Test
    fun `En speil relatert søknad for behandlingstema ab0455`() {
        val soknadId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(soknadId))
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.OpprettSpeilRelatert, soknadId))

        oppgaveOpprettelse.behandleOppgaver()
        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()

        verify(oppgaveService).opprettOppgave(captor.capture())
        assertThat(captor.firstValue.behandlingstema).isEqualTo("ab0455")
    }

    @Test
    fun `En ikke speil relatert søknad for behandlingstema ab0061`() {
        val soknadId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(soknadId))
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))

        oppgaveOpprettelse.behandleOppgaver()
        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()

        verify(oppgaveService).opprettOppgave(captor.capture())
        assertThat(captor.firstValue.behandlingstema).isEqualTo("ab0061")
    }

    private fun leggSøknadPåKafka(søknad: SykepengesoknadDTO) =
        aivenSoknadSendtListener.listen(skapConsumerRecord("key", søknad.serialisertTilString()), acknowledgment)

    private fun leggOppgavePåAivenKafka(oppgave: OppgaveDTO) =
        aivenSpreOppgaverListener.listen(skapConsumerRecord("key", oppgave.serialisertTilString()), acknowledgment)

    private fun søknad(
        soknadId: UUID = UUID.randomUUID(),
        sendtNav: LocalDateTime? = LocalDateTime.now(),
        sendtArbeidsgiver: LocalDateTime? = null
    ) = SykepengesoknadDTO(
        fnr = fnr,
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
}

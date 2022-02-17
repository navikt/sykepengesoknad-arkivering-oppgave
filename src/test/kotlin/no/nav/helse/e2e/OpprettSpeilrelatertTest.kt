package no.nav.helse.e2e

import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.helse.FellesTestoppsett
import no.nav.helse.any
import no.nav.helse.arkivering.Arkivaren
import no.nav.helse.client.DokArkivClient
import no.nav.helse.client.SyfosoknadClient
import no.nav.helse.client.pdl.PdlClient
import no.nav.helse.domain.DokumentTypeDTO
import no.nav.helse.domain.OppdateringstypeDTO
import no.nav.helse.domain.OppgaveDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvartypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.helse.kafka.consumer.AivenSoknadSendtListener
import no.nav.helse.kafka.consumer.AivenSpreOppgaverListener
import no.nav.helse.objectMapper
import no.nav.helse.serialisertTilString
import no.nav.helse.service.*
import no.nav.helse.skapConsumerRecord
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
        val aktørId = "aktørId"
        val fnr = "fnr"
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
    lateinit var dokArkivClient: DokArkivClient

    @MockBean
    lateinit var acknowledgment: Acknowledgment

    @MockBean
    lateinit var syfosoknadConsumer: SyfosoknadClient

    @Autowired
    lateinit var aivenSoknadSendtListener: AivenSoknadSendtListener

    @Autowired
    lateinit var aivenSpreOppgaverListener: AivenSpreOppgaverListener

    @Autowired
    lateinit var behandleVedTimeoutService: BehandleVedTimeoutService

    @BeforeEach
    fun setup() {
        whenever(identService.hentAktorIdForFnr(any())).thenReturn(aktørId)
        whenever(identService.hentFnrForAktorId(any())).thenReturn(fnr)
        whenever(pdlClient.hentFormattertNavn(any())).thenReturn("Kalle Klovn")
        whenever(arkivaren.opprettJournalpost(any())).thenReturn("jpost1234")
        whenever(oppgaveService.opprettOppgave(any())).thenReturn(OppgaveResponse(123))
        whenever(syfosoknadConsumer.hentSoknad(any())).thenReturn(
            objectMapper.readValue(
                søknad().serialisertTilString(),
                SykepengesoknadDTO::class.java
            )
        )
    }

    @Test
    fun `En speil relatert søknad for behandlingstema ab0455`() {
        val søknadsId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(søknadsId))
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.OpprettSpeilRelatert, søknadsId))

        behandleVedTimeoutService.behandleTimeout()
        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()

        verify(oppgaveService).opprettOppgave(captor.capture())
        assertThat(captor.firstValue.behandlingstema).isEqualTo("ab0455")
    }

    @Test
    fun `En ikke speil relatert søknad for behandlingstema ab0061`() {
        val søknadsId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(søknadsId))
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, søknadsId))

        behandleVedTimeoutService.behandleTimeout()
        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()

        verify(oppgaveService).opprettOppgave(captor.capture())
        assertThat(captor.firstValue.behandlingstema).isEqualTo("ab0061")
    }

    private fun leggSøknadPåKafka(søknad: SykepengesoknadDTO) =
        aivenSoknadSendtListener.listen(skapConsumerRecord("key", søknad.serialisertTilString()), acknowledgment)

    private fun leggOppgavePåAivenKafka(oppgave: OppgaveDTO) =
        aivenSpreOppgaverListener.listen(skapConsumerRecord("key", oppgave.serialisertTilString()), acknowledgment)

    private fun søknad(
        søknadsId: UUID = UUID.randomUUID(),
        sendtNav: LocalDateTime? = LocalDateTime.now(),
        sendtArbeidsgiver: LocalDateTime? = null
    ) = SykepengesoknadDTO(
        fnr = fnr,
        id = søknadsId.toString(),
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

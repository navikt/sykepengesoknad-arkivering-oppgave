package no.nav.syfo.e2e

import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.TestApplication
import no.nav.syfo.any
import no.nav.syfo.arkivering.Arkivaren
import no.nav.syfo.client.DokArkivClient
import no.nav.syfo.client.SyfosoknadClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.kafka.consumer.AivenSoknadSendtListener
import no.nav.syfo.kafka.consumer.AivenSpreOppgaverListener
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SporsmalDTO
import no.nav.syfo.kafka.felles.SvarDTO
import no.nav.syfo.kafka.felles.SvartypeDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.objectMapper
import no.nav.syfo.serialisertTilString
import no.nav.syfo.service.*
import no.nav.syfo.skapConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
@EnableMockOAuth2Server
class OpprettSpeilrelatertTest : AbstractContainerBaseTest() {

    companion object {
        val aktørId = "aktørId"
        val fnr = "fnr"
    }

    @MockBean
    lateinit var oppgaveService: OppgaveService

    @MockBean
    lateinit var arkivaren: Arkivaren

    @MockBean
    lateinit var behandlendeEnhetService: BehandlendeEnhetService

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

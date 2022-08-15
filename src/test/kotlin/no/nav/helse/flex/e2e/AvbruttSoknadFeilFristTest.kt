package no.nav.helse.flex.e2e

import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.any
import no.nav.helse.flex.arkivering.Arkivaren
import no.nav.helse.flex.client.pdl.PdlClient
import no.nav.helse.flex.kafka.consumer.AivenSoknadSendtListener
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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@DirtiesContext
class AvbruttSoknadFeilFristTest : FellesTestoppsett() {

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
    lateinit var acknowledgment: Acknowledgment

    @Autowired
    lateinit var aivenSoknadSendtListener: AivenSoknadSendtListener

    @Test
    fun `Søknad med merknad om feil frist får tekst i oppgaven`() {

        whenever(identService.hentAktorIdForFnr(any())).thenReturn(aktørId)
        whenever(identService.hentFnrForAktorId(any())).thenReturn(fnr)
        whenever(pdlClient.hentFormattertNavn(any())).thenReturn("Kalle Klovn")
        whenever(arkivaren.opprettJournalpost(any())).thenReturn("jpost1234")
        whenever(oppgaveService.opprettOppgave(any())).thenReturn(OppgaveResponse(123, "4488", "SYK", "SOK"))

        val soknad = SykepengesoknadDTO(
            fnr = fnr,
            id = UUID.randomUUID().toString(),
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
            sendtNav = LocalDateTime.now(),
            sendtArbeidsgiver = null,
            sendTilGosys = true,
            merknader = listOf("AVBRUTT_FEILINFO")
        )
        leggSøknadPåKafka(soknad)

        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()

        verify(oppgaveService).opprettOppgave(captor.capture())
        val oppgaveRequest = captor.firstValue
        assertThat(oppgaveRequest.beskrivelse).startsWith("Ved en feil har brukeren, for akkurat denne søknadsperioden, fått beskjed om en annen frist enn hva folketrygdloven § 22-13 tredje ledd tilsier.")
    }

    private fun leggSøknadPåKafka(søknad: SykepengesoknadDTO) =
        aivenSoknadSendtListener.listen(skapConsumerRecord("key", søknad.serialisertTilString()), acknowledgment)
}

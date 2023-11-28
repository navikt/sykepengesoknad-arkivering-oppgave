package no.nav.helse.flex.e2e

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.*
import no.nav.helse.flex.mockdispatcher.SykepengesoknadMockDispatcher
import no.nav.helse.flex.service.*
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvartypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit

@DirtiesContext
class AvbruttSoknadFeilFristTest : FellesTestoppsett() {

    val fnr = "fnr"

    @Test
    fun `Søknad med merknad om feil frist får tekst i oppgaven`() {
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

        SykepengesoknadMockDispatcher.enque(soknad)
        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.beskrivelse).startsWith("Ved en feil har brukeren, for akkurat denne søknadsperioden, fått beskjed om en annen frist enn hva folketrygdloven § 22-13 tredje ledd tilsier.")
    }
}

package no.nav.helse.flex.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.FellesTestOppsett
import no.nav.helse.flex.any
import no.nav.helse.flex.arkivering.Arkivaren
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.InnsendingDbRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@DirtiesContext
class SaksbehandlingsServiceTest : FellesTestOppsett() {
    @MockitoSpyBean
    lateinit var arkivaren: Arkivaren

    @Autowired
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @Test
    fun behandlerSoknaderSomEttersendesTilNavDerDetManglerOppgave() {
        val now = LocalDateTime.now()
        val sykepengesoknadUtenOppgave =
            objectMapper.readValue(this::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(sendtNav = null, sendtArbeidsgiver = now)
        val sykepengesoknadEttersendingTilNAV =
            objectMapper.readValue(this::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
                .copy(sendtNav = now, sendtArbeidsgiver = now)

        val oppgaveRequestFør = oppgaveMockWebserver.requestCount
        saksbehandlingsService.behandleSoknad(sykepengesoknadUtenOppgave)
        verify(arkivaren, times(1)).opprettJournalpost(any())
        assertThat(oppgaveMockWebserver.requestCount).isEqualTo(oppgaveRequestFør)

        saksbehandlingsService.behandleSoknad(sykepengesoknadEttersendingTilNAV)
        saksbehandlingsService.opprettOppgave(sykepengesoknadEttersendingTilNAV, innsending(sykepengesoknadEttersendingTilNAV))
        verify(arkivaren, times(1)).opprettJournalpost(any())
        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")
    }

    @Test
    fun `oppretter oppgave med korrekte felter`() {
        val søknad = objectMapper.readValue(this::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)

        saksbehandlingsService.opprettOppgave(søknad, innsending(søknad))
        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")

        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.journalpostId).isEqualTo("journalpostId")
        assertThat(oppgaveRequestBody.beskrivelse).isEqualTo(
            """
            Søknad om sykepenger for perioden 16.10.2018 til 24.10.2018

            Arbeidsgiver: ARBEIDSGIVER A/S
            Organisasjonsnummer: 1257358

            Periode 1:
            16.10.2018 - 20.10.2018
            Grad: 100

            Periode 2:
            21.10.2018 - 24.10.2018
            Grad: 40

            Betaler arbeidsgiveren lønnen din når du er syk?
            Vet ikke
            """.trimIndent(),
        )
        assertThat(oppgaveRequestBody.tema).isEqualTo("SYK")
        assertThat(oppgaveRequestBody.oppgavetype).isEqualTo("SOK")
        assertThat(oppgaveRequestBody.prioritet).isEqualTo("NORM")
        assertThat(oppgaveRequestBody.behandlingstema).isEqualTo("ab0061")
    }

    private fun innsending(sykepengesoknadUtenOppgave: Sykepengesoknad): InnsendingDbRecord {
        return InnsendingDbRecord(
            id = "innsending-guid",
            sykepengesoknadId = sykepengesoknadUtenOppgave.id,
            journalpostId = "journalpostId",
            oppgaveId = null,
            behandlet = Instant.now(),
        )
    }
}

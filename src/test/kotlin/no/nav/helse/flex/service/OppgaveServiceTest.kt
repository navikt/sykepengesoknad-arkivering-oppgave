package no.nav.helse.flex.service

import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Arbeidssituasjon
import no.nav.helse.flex.domain.dto.Soknadstype
import okhttp3.mockwebserver.MockResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate.now
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters.next

class OppgaveServiceTest : FellesTestoppsett() {
    @Autowired
    lateinit var oppgaveClient: OppgaveClient

    private val aktorId = "aktorId"
    private val journalpostId = "145"

    @Test
    fun innsendingLordagOgSondagGirSammeFristSomMandag() {
        assertThat(OppgaveClient.omTreUkedager(now().with(next(SATURDAY))).dayOfWeek).isEqualTo(THURSDAY)
        assertThat(OppgaveClient.omTreUkedager(now().with(next(SUNDAY))).dayOfWeek).isEqualTo(THURSDAY)
        assertThat(OppgaveClient.omTreUkedager(now().with(next(MONDAY))).dayOfWeek).isEqualTo(THURSDAY)
    }

    @Test
    fun fristSettesOmTreDagerUtenomHelg() {
        assertThat(OppgaveClient.omTreUkedager(now().with(next(MONDAY))).dayOfWeek).isEqualTo(THURSDAY)
        assertThat(OppgaveClient.omTreUkedager(now().with(next(TUESDAY))).dayOfWeek).isEqualTo(FRIDAY)
    }

    @Test
    fun toDagerLeggesTilOverHelg() {
        assertThat(OppgaveClient.omTreUkedager(now().with(next(WEDNESDAY))).dayOfWeek).isEqualTo(MONDAY)
        assertThat(OppgaveClient.omTreUkedager(now().with(next(THURSDAY))).dayOfWeek).isEqualTo(TUESDAY)
        assertThat(OppgaveClient.omTreUkedager(now().with(next(FRIDAY))).dayOfWeek).isEqualTo(WEDNESDAY)
    }

    @Test
    fun opprettOppgaveGirFeilmeldingHvisOppgaveErNede() {
        oppgaveMockWebserver.enqueue(MockResponse().setResponseCode(500))

        assertThrows(RuntimeException::class.java) {
            val oppgaveRequest = OppgaveClient.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.ARBEIDSTAKERE))
            oppgaveClient.opprettOppgave(oppgaveRequest)
        }
    }

    @Test
    fun lagRequestBodyLagerRequestMedRiktigeFelter() {
        val body = OppgaveClient.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.ARBEIDSTAKERE))

        assertThat(body.tildeltEnhetsnr).isEqualTo(null)
        assertThat(body.opprettetAvEnhetsnr).isEqualTo("9999")
        assertThat(body.aktoerId).isEqualTo(aktorId)
        assertThat(body.journalpostId).isEqualTo(journalpostId)
        assertThat(body.beskrivelse).isNotEmpty()
        assertThat(body.tema).isEqualTo("SYK")
        assertThat(body.behandlingstema).isEqualTo("ab0061")
        assertThat(body.oppgavetype).isEqualTo("SOK")
        assertThat(body.aktivDato).isNotEmpty()
        assertThat(body.fristFerdigstillelse).isNotEmpty()
        assertThat(body.prioritet).isEqualTo("NORM")
    }

    @Test
    fun lagRequestBodySetterRiktigBehandlingstema() {
        val utland = OppgaveClient.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.OPPHOLD_UTLAND))
        val arbeidstaker = OppgaveClient.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.ARBEIDSTAKERE))
        val arbeidsledig = OppgaveClient.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.ARBEIDSLEDIG))
        val behandlingsdager = OppgaveClient.lagRequestBody(aktorId, journalpostId, lagSoknad(Soknadstype.BEHANDLINGSDAGER))
        val redusertVenteperiode =
            OppgaveClient.lagRequestBody(
                aktorId,
                journalpostId,
                lagSoknad(Soknadstype.SELVSTENDIGE_OG_FRILANSERE),
                harRedusertVenteperiode = true,
            )

        assertThat(utland.behandlingstema).isEqualTo("ab0314")
        assertThat(arbeidstaker.behandlingstema).isEqualTo("ab0061")
        assertThat(arbeidsledig.behandlingstema).isEqualTo("ab0426")
        assertThat(behandlingsdager.behandlingstema).isEqualTo("ab0351")
        assertThat(redusertVenteperiode.behandlingstype).isEqualTo("ae0247")
    }

    private fun lagSoknad(soknadstype: Soknadstype): Soknad {
        return Soknad(
            aktorId = aktorId,
            soknadsId = "",
            fnr = "fnr",
            navn = "Navn",
            tilNav = true,
            soknadstype = soknadstype,
            opprettet = LocalDateTime.now(),
            fom = now().minusWeeks(3),
            tom = now().minusDays(3),
            innsendtTid = null,
            sendtArbeidsgiver = null,
            startSykeforlop = now().minusWeeks(3),
            sykmeldingUtskrevet = now().minusWeeks(3),
            arbeidsgiver = "arbeidsgiver",
            korrigerer = null,
            korrigertAv = null,
            arbeidssituasjon = Arbeidssituasjon.ARBEIDSTAKER,
            soknadPerioder = ArrayList(),
            sporsmal = ArrayList(),
            avsendertype = null,
            merknaderFraSykmelding = null,
            egenmeldingsdagerFraSykmelding = null,
        )
    }
}

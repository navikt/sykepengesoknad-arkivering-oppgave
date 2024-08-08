package no.nav.helse.flex.medlemskap

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.FellesTestOppsett
import no.nav.helse.flex.domain.dto.Arbeidssituasjon
import no.nav.helse.flex.domain.dto.SoknadPeriode
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sporsmal
import no.nav.helse.flex.domain.dto.Svar
import no.nav.helse.flex.domain.dto.Svartype
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.domain.dto.Visningskriterie
import no.nav.helse.flex.medlemskap.EndeligVurderingResponse.MedlemskapVurderingStatus
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.BEHANDLINGSTEMA_MEDLEMSKAP
import no.nav.helse.flex.service.BEHANDLINGSTEMA_SYKEPENGER
import no.nav.helse.flex.service.OppgaveRequest
import no.nav.helse.flex.service.SaksbehandlingsService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class MedlemskapVurderingTest : FellesTestOppsett() {
    val fnr = "12121234343"
    val fom: LocalDate = LocalDate.of(2024, 5, 1)
    val tom: LocalDate = LocalDate.of(2024, 5, 31)

    @Autowired
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @AfterEach
    fun slettFraDatabase() {
        innsendingRepository.deleteAll()
        medlemskapVurderingRepository.deleteAll()
    }

    @Test
    fun `Søknad med inngående UAVKLART med brukerspørsmål og endelig UAVKLART har UAVKLART i Gosys-oppgave`() {
        val soknad = lagSoknad(medlemskapVurdering = "UAVKLART").copy(sporsmal = lagSporsmalOmOppholdstillatelse())
        saksbehandlingsService.behandleSoknad(soknad)

        hentLagretInngaendeVurdering(soknad) shouldBeEqualTo "UAVKLART"
        mockEndeligVurderingResponse(soknad, MedlemskapVurderingStatus.UAVKLART)

        opprettOppgaveOgValiderLovMeRequest(soknad)

        hentLagretEndeligVurdering(soknad) shouldBeEqualTo "UAVKLART"

        val oppgaveRequest = hentOgValiderOppgaveRequest()
        behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest).let { (behandlingstema, beskrivelse) ->
            behandlingstema shouldBeEqualTo BEHANDLINGSTEMA_MEDLEMSKAP
            beskrivelse shouldBeEqualTo
                """
                |$oppgaveOverskrift
                |$oppgaveMedlemskapUavklartTekst
                |$oppgaveSporsmal
                """.trimMargin()
        }
    }

    @Test
    fun `Søknad om Gradert Reisetilskudd håndteres likt Arbeidstakersøknad`() {
        val soknad =
            lagSoknad(
                medlemskapVurdering = "UAVKLART",
                soknadstype = Soknadstype.GRADERT_REISETILSKUDD,
            ).copy(sporsmal = lagSporsmalOmOppholdstillatelse())

        saksbehandlingsService.behandleSoknad(soknad)

        hentLagretInngaendeVurdering(soknad) shouldBeEqualTo "UAVKLART"
        mockEndeligVurderingResponse(soknad, MedlemskapVurderingStatus.UAVKLART)

        opprettOppgaveOgValiderLovMeRequest(soknad)

        hentLagretEndeligVurdering(soknad) shouldBeEqualTo "UAVKLART"

        val oppgaveRequest = hentOgValiderOppgaveRequest()
        behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest).let { (behandlingstema, beskrivelse) ->
            behandlingstema shouldBeEqualTo BEHANDLINGSTEMA_MEDLEMSKAP
            beskrivelse shouldBeEqualTo
                """
                |$oppgaveOverskriftMedReisetilskudd
                |$oppgaveMedlemskapUavklartTekst
                |$oppgaveSporsmal
                """.trimMargin()
        }
    }

    @Test
    fun `Søknad med inngående UAVKLART med brukerspørsmål men LovMe svar har tom body har UAVKLART i Gosys-oppgave`() {
        val soknad = lagSoknad(medlemskapVurdering = "UAVKLART").copy(sporsmal = lagSporsmalOmOppholdstillatelse())
        saksbehandlingsService.behandleSoknad(soknad)

        hentLagretInngaendeVurdering(soknad) shouldBeEqualTo "UAVKLART"
        medlemskapMockWebserver.enqueue(MockResponse().setResponseCode(500))

        opprettOppgaveOgValiderLovMeRequest(soknad)

        hentLagretEndeligVurdering(soknad) shouldBe null

        val oppgaveRequest = hentOgValiderOppgaveRequest()
        behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest).let { (behandlingstema, beskrivelse) ->
            behandlingstema shouldBeEqualTo BEHANDLINGSTEMA_MEDLEMSKAP
            beskrivelse shouldBeEqualTo
                """
                |$oppgaveOverskrift
                |$oppgaveMedlemskapUavklartTekst
                |$oppgaveSporsmal
                """.trimMargin()
        }
    }

    @Test
    fun `Søknad med inngående UAVKLART med brukerspørsmål men LovMe svarer ikke har UAVKLART i Gosys-oppgave`() {
        val soknad = lagSoknad(medlemskapVurdering = "UAVKLART").copy(sporsmal = lagSporsmalOmOppholdstillatelse())
        saksbehandlingsService.behandleSoknad(soknad)

        hentLagretInngaendeVurdering(soknad) shouldBeEqualTo "UAVKLART"
        medlemskapMockWebserver.enqueue(
            MockResponse().addHeader("Content-Type", "application/json"),
        )

        opprettOppgaveOgValiderLovMeRequest(soknad)

        hentLagretEndeligVurdering(soknad) shouldBe null

        val oppgaveRequest = hentOgValiderOppgaveRequest()
        behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest).let { (behandlingstema, beskrivelse) ->
            behandlingstema shouldBeEqualTo BEHANDLINGSTEMA_MEDLEMSKAP
            beskrivelse shouldBeEqualTo
                """
                |$oppgaveOverskrift
                |$oppgaveMedlemskapUavklartTekst
                |$oppgaveSporsmal
                """.trimMargin()
        }
    }

    @Test
    fun `Søknad med inngående UAVKLART med brukerspørsmål og endelig NEI har NEI i Gosys-oppgave`() {
        val soknad = lagSoknad(medlemskapVurdering = "UAVKLART").copy(sporsmal = lagSporsmalOmOppholdstillatelse())
        saksbehandlingsService.behandleSoknad(soknad)

        hentLagretInngaendeVurdering(soknad) shouldBeEqualTo "UAVKLART"
        mockEndeligVurderingResponse(soknad, MedlemskapVurderingStatus.NEI)

        opprettOppgaveOgValiderLovMeRequest(soknad)

        hentLagretEndeligVurdering(soknad) shouldBeEqualTo "NEI"

        val oppgaveRequest = hentOgValiderOppgaveRequest()
        behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest).let { (behandlingstema, beskrivelse) ->
            behandlingstema shouldBeEqualTo BEHANDLINGSTEMA_MEDLEMSKAP
            beskrivelse shouldBeEqualTo
                """
                |$oppgaveOverskrift
                |$oppgaveMedlemskapNeiTekst
                |$oppgaveSporsmal
                """.trimMargin()
        }
    }

    @Test
    fun `Søknad med inngående NEI har ikke endelig vurdering og har NEI i Gosys-oppgave`() {
        val soknad = lagSoknad(medlemskapVurdering = "NEI")
        saksbehandlingsService.behandleSoknad(soknad)

        hentLagretInngaendeVurdering(soknad) shouldBeEqualTo "NEI"

        saksbehandlingsService.opprettOppgave(soknad, innsendingRepository.findBySykepengesoknadId(soknad.id)!!)
        medlemskapMockWebserver.takeRequest(100, TimeUnit.MILLISECONDS) shouldBe null

        hentLagretEndeligVurdering(soknad) shouldBe null

        val oppgaveRequest = hentOgValiderOppgaveRequest()
        behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest).let { (behandlingstema, beskrivelse) ->
            behandlingstema shouldBeEqualTo BEHANDLINGSTEMA_MEDLEMSKAP
            beskrivelse shouldBeEqualTo
                """
                |$oppgaveOverskrift
                |$oppgaveMedlemskapNeiTekst
                """.trimMargin()
        }
    }

    @Test
    fun `Søknad med inngående UAVKLART med brukerspørsmål og endelig JA har ikke vurdering i Gosys-oppgave`() {
        val soknad = lagSoknad(medlemskapVurdering = "UAVKLART").copy(sporsmal = lagSporsmalOmOppholdstillatelse())
        saksbehandlingsService.behandleSoknad(soknad)

        hentLagretInngaendeVurdering(soknad) shouldBeEqualTo "UAVKLART"
        mockEndeligVurderingResponse(soknad, MedlemskapVurderingStatus.JA)

        opprettOppgaveOgValiderLovMeRequest(soknad)

        hentLagretEndeligVurdering(soknad) shouldBeEqualTo "JA"

        val oppgaveRequest = hentOgValiderOppgaveRequest()
        behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest).let { (behandlingstema, beskrivelse) ->
            behandlingstema shouldBeEqualTo BEHANDLINGSTEMA_SYKEPENGER
            beskrivelse shouldBeEqualTo
                """
                |$oppgaveOverskrift
                """.trimMargin()
        }
    }

    @Test
    fun `Søknad med inngående UAVKLART uten brukerspørsmål har ikke endelig vurdering og vurdering i Gosys-oppgave`() {
        val soknad = lagSoknad(medlemskapVurdering = "UAVKLART")
        saksbehandlingsService.behandleSoknad(soknad)

        hentLagretInngaendeVurdering(soknad) shouldBeEqualTo "UAVKLART"

        saksbehandlingsService.opprettOppgave(soknad, innsendingRepository.findBySykepengesoknadId(soknad.id)!!)
        medlemskapMockWebserver.takeRequest(100, TimeUnit.MILLISECONDS) shouldBe null

        hentLagretEndeligVurdering(soknad) shouldBe null

        val oppgaveRequest = hentOgValiderOppgaveRequest()
        behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest).let { (behandlingstema, beskrivelse) ->
            behandlingstema shouldBeEqualTo BEHANDLINGSTEMA_SYKEPENGER
            beskrivelse shouldBeEqualTo
                """
                |$oppgaveOverskrift
                """.trimMargin()
        }
    }

    @Test
    fun `Søknad med inngående JA har ikke endelig vurdering og vurdering i Gosys-oppgave`() {
        val soknad = lagSoknad(medlemskapVurdering = "JA")
        saksbehandlingsService.behandleSoknad(soknad)

        hentLagretInngaendeVurdering(soknad) shouldBeEqualTo "JA"

        saksbehandlingsService.opprettOppgave(soknad, innsendingRepository.findBySykepengesoknadId(soknad.id)!!)
        medlemskapMockWebserver.takeRequest(100, TimeUnit.MILLISECONDS) shouldBe null

        hentLagretEndeligVurdering(soknad) shouldBe null

        val oppgaveRequest = hentOgValiderOppgaveRequest()
        behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest).let { (behandlingstema, beskrivelse) ->
            behandlingstema shouldBeEqualTo BEHANDLINGSTEMA_SYKEPENGER
            beskrivelse shouldBeEqualTo
                """
                |$oppgaveOverskrift
                """.trimMargin()
        }
    }

    @Test
    fun `Søknad uten inngående vurdering blir ikke vurdert`() {
        val soknad = lagSoknad(medlemskapVurdering = null)

        saksbehandlingsService.behandleSoknad(soknad)

        medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id) shouldBeEqualTo null

        saksbehandlingsService.opprettOppgave(soknad, innsendingRepository.findBySykepengesoknadId(soknad.id)!!)
        medlemskapMockWebserver.takeRequest(100, TimeUnit.MILLISECONDS) shouldBe null

        val oppgaveRequest = hentOgValiderOppgaveRequest()
        behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest).let { (behandlingstema, beskrivelse) ->
            behandlingstema shouldBeEqualTo BEHANDLINGSTEMA_SYKEPENGER
            beskrivelse shouldBeEqualTo
                """
                |$oppgaveOverskrift
                """.trimMargin()
        }
    }

    @Test
    fun `Frilansersøknad har ikke inngående vurdering og blir ikke vurdert`() {
        val soknad =
            lagSoknad(
                medlemskapVurdering = null,
                soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE,
            ).copy(arbeidssituasjon = Arbeidssituasjon.FRILANSER)

        saksbehandlingsService.behandleSoknad(soknad)

        medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id) shouldBeEqualTo null

        saksbehandlingsService.opprettOppgave(soknad, innsendingRepository.findBySykepengesoknadId(soknad.id)!!)
        medlemskapMockWebserver.takeRequest(100, TimeUnit.MILLISECONDS) shouldBe null

        val oppgaveRequest = hentOgValiderOppgaveRequest()
        behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest).let { (behandlingstema, beskrivelse) ->
            behandlingstema shouldBeEqualTo BEHANDLINGSTEMA_SYKEPENGER
            beskrivelse shouldBeEqualTo
                """
                |$oppgaveOverskriftForFrilanser
                """.trimMargin()
        }
    }

    @Test
    fun `Alle verdier for inngaende og endeligvurdering er lagret riktig`() {
        val soknad = lagSoknad(medlemskapVurdering = "UAVKLART").copy(sporsmal = lagSporsmalOmOppholdstillatelse())
        saksbehandlingsService.behandleSoknad(soknad)

        val inngaendeVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)!!
        inngaendeVurdering.sykepengesoknadId shouldBeEqualTo soknad.id
        inngaendeVurdering.fnr shouldBeEqualTo soknad.fnr
        inngaendeVurdering.fom shouldBeEqualTo soknad.fom
        inngaendeVurdering.tom shouldBeEqualTo soknad.tom
        inngaendeVurdering.inngaendeVurdering shouldBeEqualTo "UAVKLART"
        inngaendeVurdering.vurderingId shouldBeEqualTo null
        inngaendeVurdering.endeligVurdering shouldBeEqualTo null

        mockEndeligVurderingResponse(soknad, MedlemskapVurderingStatus.UAVKLART)
        opprettOppgaveOgValiderLovMeRequest(soknad)

        oppgaveMockWebserver.takeRequest(100, TimeUnit.MILLISECONDS)!!

        val endeligVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)!!
        endeligVurdering.inngaendeVurdering shouldBeEqualTo "UAVKLART"
        endeligVurdering.vurderingId shouldBeEqualTo "vurderingId"
        endeligVurdering.endeligVurdering shouldBeEqualTo "UAVKLART"
    }

    private fun lagSoknad(
        medlemskapVurdering: String?,
        soknadstype: Soknadstype = Soknadstype.ARBEIDSTAKERE,
    ) = Sykepengesoknad(
        fnr = fnr,
        aktorId = "aktor-$fnr",
        id = UUID.randomUUID().toString(),
        opprettet = LocalDateTime.now(),
        fom = fom,
        tom = tom,
        soknadPerioder = listOf(SoknadPeriode(fom, tom, 100)),
        soknadstype = soknadstype,
        sporsmal = emptyList(),
        egenmeldingsdagerFraSykmelding = emptyList(),
        status = "SENDT",
        sendtNav = LocalDateTime.now(),
        medlemskapVurdering = medlemskapVurdering,
    )

    private fun hentLagretInngaendeVurdering(soknad: Sykepengesoknad) =
        medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)!!.inngaendeVurdering

    private fun mockEndeligVurderingResponse(
        soknad: Sykepengesoknad,
        medlemskapVurderingStatus: MedlemskapVurderingStatus,
    ) {
        medlemskapMockWebserver.enqueue(
            MockResponse().setBody(
                lagEndeligVurderingResponse(soknad.id, medlemskapVurderingStatus),
            ).addHeader("Content-Type", "application/json"),
        )
    }

    private fun opprettOppgaveOgValiderLovMeRequest(soknad: Sykepengesoknad) {
        saksbehandlingsService.opprettOppgave(soknad, innsendingRepository.findBySykepengesoknadId(soknad.id)!!)
        medlemskapMockWebserver.takeRequest().toString() shouldBeEqualTo "POST /flexvurdering HTTP/1.1"
    }

    private fun hentOgValiderOppgaveRequest(): RecordedRequest {
        val oppgaveRequest = oppgaveMockWebserver.takeRequest(100, TimeUnit.MILLISECONDS)!!
        oppgaveRequest.requestLine shouldBeEqualTo "POST /api/v1/oppgaver HTTP/1.1"
        return oppgaveRequest
    }

    private fun hentLagretEndeligVurdering(soknad: Sykepengesoknad) =
        medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)!!.endeligVurdering

    private fun lagEndeligVurderingResponse(
        sykepengesoknadId: String,
        endeligVurderingStatus: MedlemskapVurderingStatus,
    ) = EndeligVurderingResponse(
        sykepengesoknadId,
        "vurderingId",
        fnr,
        fom,
        tom,
        endeligVurderingStatus,
    ).serialisertTilString()

    private fun lagSporsmalOmOppholdstillatelse() =
        listOf(
            Sporsmal(
                id = UUID.randomUUID().toString(),
                tag = "MEDLEMSKAP_OPPHOLDSTILLATELSE_V2",
                sporsmalstekst = "Har Utlendingsdirektoratet gitt deg en oppholdstillatelse før 1. mai 2024?",
                svartype = Svartype.JA_NEI,
                svar = listOf(Svar(verdi = "JA")),
                kriterieForVisningAvUndersporsmal = Visningskriterie.JA,
                undersporsmal =
                    listOf(
                        Sporsmal(
                            id = UUID.randomUUID().toString(),
                            tag = "MEDLEMSKAP_OPPHOLDSTILLATELSE_VEDTAKSDATO",
                            sporsmalstekst = "Hvilken dato fikk du denne oppholdstillatelsen?",
                            svartype = Svartype.DATO,
                            svar = listOf(Svar(verdi = "2023-01-01")),
                            min = "2014-01-01",
                            max = "2024-12-31",
                        ),
                        Sporsmal(
                            id = UUID.randomUUID().toString(),
                            tag = "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERIODE",
                            sporsmalstekst = "Hvilken periode gjelder denne oppholdstillatelsen?",
                            svar = listOf(Svar(verdi = "{\"fom\":\"2023-01-01\",\"tom\":\"2023-12-31\"}")),
                            svartype = Svartype.PERIODE,
                            min = "2014-01-01",
                            max = "2024-12-31",
                        ),
                    ),
            ),
        )

    private fun behandlingsteamOgBeskrivelseFraOppgaveRequest(oppgaveRequest: RecordedRequest): Pair<String, String> {
        val oppgaveRequestData = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        return Pair(oppgaveRequestData.behandlingstema!!, oppgaveRequestData.beskrivelse)
    }

    val oppgaveOverskrift =
        """
        Søknad om sykepenger for perioden 01.05.2024 til 31.05.2024
        
        Periode 1:
        01.05.2024 - 31.05.2024
        Grad: 100
        
        """.trimIndent()

    val oppgaveOverskriftForFrilanser =
        """
        Søknad om sykepenger for frilanser for perioden 01.05.2024 til 31.05.2024
        
        Periode 1:
        01.05.2024 - 31.05.2024
        Grad: 100
        
        """.trimIndent()

    val oppgaveOverskriftMedReisetilskudd =
        """
        Søknad om sykepenger med reisetilskudd for perioden 01.05.2024 til 31.05.2024
        
        Periode 1:
        01.05.2024 - 31.05.2024
        Grad: 100
        
        """.trimIndent()

    val oppgaveMedlemskapUavklartTekst =
        """
        Om bruker er medlem i folketrygden eller ikke, kunne ikke avklares automatisk.
        Medlemskap status: UAVKLART
        
        Du må se på svarene til bruker.
        Informasjon om hva du skal gjøre finner du på Navet, se
        https://navno.sharepoint.com/sites/fag-og-ytelser-eos-lovvalg-medlemskap/SitePages/Hvordan-vurderer-jeg-lovvalg-og-medlemskap.aspx
        
        """.trimIndent()

    val oppgaveMedlemskapNeiTekst =
        """ 
        Om bruker er medlem i folketrygden eller ikke er automatisk avklart.
        Medlemskap status: NEI

        Se på medlemskapsfanen i Gosys for å finne riktig periode.
        Se på dokumentet i Gosys for å finne arbeidsgiver.
        Se i Aa-registeret om bruker har samme arbeidsgiver som i vedtaket/A1 fra utlandet.
        Hvis Ja: Bruker er ikke medlem. Hvis Nei: Kontakt bruker/arbeidsgiver for å avklare brukers situasjon.
        
        """.trimIndent()

    val oppgaveSporsmal =
        """
        Har Utlendingsdirektoratet gitt deg en oppholdstillatelse før 1. mai 2024?
        Ja
            Hvilken dato fikk du denne oppholdstillatelsen?
            01.01.2023
        
            Hvilken periode gjelder denne oppholdstillatelsen?
            01.01.2023 - 31.12.2023
        """.trimIndent()
}

package no.nav.helse.flex.medlemskap

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.domain.dto.*
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.*
import okhttp3.mockwebserver.MockResponse
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class MedlemskapVurderingTest : FellesTestoppsett() {

    val fnr = "12121234343"
    val fom = LocalDate.of(2023, 9, 1)
    val tom = LocalDate.of(2023, 9, 20)

    @Autowired
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @Autowired
    lateinit var medlemskapVurderingRepository: MedlemskapVurderingRepository

    @Autowired
    lateinit var innsendingRepository: InnsendingRepository

    @BeforeAll
    fun unleashToggle() {
        fakeUnleash.enable(UNLEASH_CONTEXT_MEDLEMSKAP_SPORSMAL)
    }

    @AfterEach
    fun opprydding() {
        innsendingRepository.deleteAll()
        medlemskapVurderingRepository.deleteAll()
    }

    @Test
    fun `En frilansersøknad, blir ikke vurdert`() {
        val soknad = soknad(medlemskapVurdering = null, soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE)

        saksbehandlingsService.behandleSoknad(soknad)

        medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id) shouldBeEqualTo null
    }

    @Test
    fun `En arbeidstakersøknad uten inngående medlemskapvurdering, blir ikke vurdert`() {
        val soknad = soknad(medlemskapVurdering = null)

        saksbehandlingsService.behandleSoknad(soknad)

        medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id) shouldBeEqualTo null
    }

    @Test
    fun `En arbeidstakersøknad som har inngående medlemskap vurdering UAVKLART og endelig vurdering NEI`() {
        val soknad = soknad(medlemskapVurdering = "UAVKLART").copy(sporsmal = medlemskapSporsmal())
        saksbehandlingsService.behandleSoknad(soknad)

        val inngåendeVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)
        inngåendeVurdering shouldNotBe null
        inngåendeVurdering!!.sykepengesoknadId shouldBeEqualTo soknad.id
        inngåendeVurdering.fnr shouldBeEqualTo soknad.fnr
        inngåendeVurdering.fom shouldBeEqualTo soknad.fom
        inngåendeVurdering.tom shouldBeEqualTo soknad.tom
        inngåendeVurdering.inngaendeVurdering shouldBeEqualTo "UAVKLART"
        inngåendeVurdering.vurderingId shouldBeEqualTo null
        inngåendeVurdering.endeligVurdering shouldBeEqualTo null

        val innsending = innsendingRepository.findBySykepengesoknadId(soknad.id)!!
        innsending.sykepengesoknadId shouldBeEqualTo soknad.id
        innsending.journalpostId shouldNotBe null

        medlemskapMockWebserver.enqueue(
            MockResponse().setBody(
                EndeligVurderingResponse(
                    soknad.id,
                    "vurderingId",
                    fnr,
                    fom,
                    tom,
                    EndeligVurderingResponse.MedlemskapVurderingStatus.NEI
                ).serialisertTilString()
            ).addHeader("Content-Type", "application/json")
        )
        saksbehandlingsService.opprettOppgave(soknad, innsending)

        val endeligVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)!!
        endeligVurdering.inngaendeVurdering shouldBeEqualTo "UAVKLART"
        endeligVurdering.vurderingId shouldBeEqualTo "vurderingId"
        endeligVurdering.endeligVurdering shouldBeEqualTo "NEI"

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        oppgaveRequest.requestLine shouldBeEqualTo "POST /api/v1/oppgaver HTTP/1.1"
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        oppgaveRequestBody.behandlingstema shouldBeEqualTo "ab0269"
        oppgaveRequestBody.beskrivelse shouldBeEqualTo """
        Søknad om sykepenger for perioden 01.09.2023 - 20.09.2023

        Periode 1:
        01.09.2023 - 20.09.2023
        Grad: 100
        
        Om bruker er medlem i folketrygden eller ikke, kunne ikke avklares automatisk.
        Medlemskap status: NEI
        
        Du må se på svarene til bruker.
        Informasjon om hva du skal gjøre finner du på Navet, se
        https://navno.sharepoint.com/sites/fag-og-ytelser-eos-lovvalg-medlemskap/SitePages/Hvordan-vurderer-jeg-lovvalg-og-medlemskap.aspx
        
        Har du oppholdstillatelse fra utlendingsdirektoratet?
        Ja
            Når fikk du vedtak om oppholdstillatelse?
            01.01.2023
        
            Har du fått permanent oppholdstillatelse?
            Nei
                Hvilken periode har du fått oppholdstillatelse?
                13.12.2022 - 02.01.2023
        """.trimIndent()
    }

    @Test
    fun `En arbeidstakersøknad som har inngående og endelig medlemskap vurdering UAVKLART`() {
        val soknad = soknad(medlemskapVurdering = "UAVKLART").copy(sporsmal = medlemskapSporsmal())
        saksbehandlingsService.behandleSoknad(soknad)

        val inngåendeVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)
        inngåendeVurdering shouldNotBe null
        inngåendeVurdering!!.sykepengesoknadId shouldBeEqualTo soknad.id
        inngåendeVurdering.fnr shouldBeEqualTo soknad.fnr
        inngåendeVurdering.fom shouldBeEqualTo soknad.fom
        inngåendeVurdering.tom shouldBeEqualTo soknad.tom
        inngåendeVurdering.inngaendeVurdering shouldBeEqualTo "UAVKLART"
        inngåendeVurdering.vurderingId shouldBeEqualTo null
        inngåendeVurdering.endeligVurdering shouldBeEqualTo null

        val innsending = innsendingRepository.findBySykepengesoknadId(soknad.id)!!
        innsending.sykepengesoknadId shouldBeEqualTo soknad.id
        innsending.journalpostId shouldNotBe null

        medlemskapMockWebserver.enqueue(
            MockResponse().setBody(
                EndeligVurderingResponse(
                    soknad.id,
                    "vurderingId",
                    fnr,
                    fom,
                    tom,
                    EndeligVurderingResponse.MedlemskapVurderingStatus.UAVKLART
                ).serialisertTilString()
            ).addHeader("Content-Type", "application/json")
        )
        saksbehandlingsService.opprettOppgave(soknad, innsending)

        val endeligVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)!!
        endeligVurdering.inngaendeVurdering shouldBeEqualTo "UAVKLART"
        endeligVurdering.vurderingId shouldBeEqualTo "vurderingId"
        endeligVurdering.endeligVurdering shouldBeEqualTo "UAVKLART"

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        oppgaveRequest.requestLine shouldBeEqualTo "POST /api/v1/oppgaver HTTP/1.1"
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        oppgaveRequestBody.behandlingstema shouldBeEqualTo "ab0269"
        oppgaveRequestBody.beskrivelse shouldBeEqualTo """
        Søknad om sykepenger for perioden 01.09.2023 - 20.09.2023

        Periode 1:
        01.09.2023 - 20.09.2023
        Grad: 100
        
        Om bruker er medlem i folketrygden eller ikke, kunne ikke avklares automatisk.
        Medlemskap status: UAVKLART
        
        Du må se på svarene til bruker.
        Informasjon om hva du skal gjøre finner du på Navet, se
        https://navno.sharepoint.com/sites/fag-og-ytelser-eos-lovvalg-medlemskap/SitePages/Hvordan-vurderer-jeg-lovvalg-og-medlemskap.aspx
        
        Har du oppholdstillatelse fra utlendingsdirektoratet?
        Ja
            Når fikk du vedtak om oppholdstillatelse?
            01.01.2023
        
            Har du fått permanent oppholdstillatelse?
            Nei
                Hvilken periode har du fått oppholdstillatelse?
                13.12.2022 - 02.01.2023
        """.trimIndent()
    }

    @Test
    fun `En arbeidstakersøknad med endelig vurdering JA, oppretter vanlig gosys oppgave`() {
        val soknad = soknad(medlemskapVurdering = "UAVKLART")
        saksbehandlingsService.behandleSoknad(soknad)

        val inngåendeVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)!!
        inngåendeVurdering.fom shouldBeEqualTo soknad.fom
        inngåendeVurdering.tom shouldBeEqualTo soknad.tom
        inngåendeVurdering.inngaendeVurdering shouldBeEqualTo "UAVKLART"

        val innsending = innsendingRepository.findBySykepengesoknadId(soknad.id)!!

        medlemskapMockWebserver.enqueue(
            MockResponse().setBody(
                EndeligVurderingResponse(
                    soknad.id,
                    "vurderingId",
                    fnr,
                    fom,
                    tom,
                    EndeligVurderingResponse.MedlemskapVurderingStatus.JA
                ).serialisertTilString()
            ).addHeader("Content-Type", "application/json")
        )
        saksbehandlingsService.opprettOppgave(soknad, innsending)

        val endeligVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)!!
        endeligVurdering.inngaendeVurdering shouldBeEqualTo "UAVKLART"
        endeligVurdering.vurderingId shouldBeEqualTo "vurderingId"
        endeligVurdering.endeligVurdering shouldBeEqualTo "JA"

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        oppgaveRequest.requestLine shouldBeEqualTo "POST /api/v1/oppgaver HTTP/1.1"
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        oppgaveRequestBody.behandlingstema shouldBeEqualTo "ab0061"
        oppgaveRequestBody.beskrivelse shouldBeEqualTo """
        Søknad om sykepenger for perioden 01.09.2023 - 20.09.2023

        Periode 1:
        01.09.2023 - 20.09.2023
        Grad: 100
        
        Spørsmål
        Nei
        """.trimIndent()
    }

    @Test
    fun `Når LovMe ikke finner inngående vurdring, oppretter vanlig gosys oppgave`() {
        val soknad = soknad(medlemskapVurdering = "UAVKLART")
        saksbehandlingsService.behandleSoknad(soknad)

        val inngåendeVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)!!
        inngåendeVurdering.fom shouldBeEqualTo soknad.fom
        inngåendeVurdering.tom shouldBeEqualTo soknad.tom
        inngåendeVurdering.inngaendeVurdering shouldBeEqualTo "UAVKLART"

        val innsending = innsendingRepository.findBySykepengesoknadId(soknad.id)!!

        medlemskapMockWebserver.enqueue(MockResponse().setResponseCode(404))
        saksbehandlingsService.opprettOppgave(soknad, innsending)

        val endeligVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(soknad.id)!!
        endeligVurdering.inngaendeVurdering shouldBeEqualTo "UAVKLART"
        endeligVurdering.vurderingId shouldBeEqualTo null
        endeligVurdering.endeligVurdering shouldBeEqualTo null

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        oppgaveRequest.requestLine shouldBeEqualTo "POST /api/v1/oppgaver HTTP/1.1"
        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        oppgaveRequestBody.behandlingstema shouldBeEqualTo "ab0061"
        oppgaveRequestBody.beskrivelse shouldBeEqualTo """
        Søknad om sykepenger for perioden 01.09.2023 - 20.09.2023

        Periode 1:
        01.09.2023 - 20.09.2023
        Grad: 100
        
        Spørsmål
        Nei
        """.trimIndent()
    }

    private fun soknad(
        medlemskapVurdering: String?,
        soknadstype: Soknadstype = Soknadstype.ARBEIDSTAKERE
    ) = Sykepengesoknad(
        fnr = fnr,
        aktorId = "aktor-$fnr",
        id = UUID.randomUUID().toString(),
        opprettet = LocalDateTime.now(),
        fom = fom,
        tom = tom,
        soknadPerioder = listOf(
            SoknadPeriode(fom, tom, 100)
        ),
        soknadstype = soknadstype,
        sporsmal = listOf(
            Sporsmal(
                id = UUID.randomUUID().toString(),
                tag = "FRISKMELDT",
                sporsmalstekst = "Spørsmål",
                svartype = Svartype.JA_NEI,
                svar = listOf(Svar(verdi = "NEI"))
            )
        ),
        egenmeldingsdagerFraSykmelding = emptyList(),
        status = "SENDT",
        sendtNav = LocalDateTime.now(),
        medlemskapVurdering = medlemskapVurdering
    )

    private fun medlemskapSporsmal() = listOf(
        Sporsmal(
            id = UUID.randomUUID().toString(),
            tag = "MEDLEMSKAP_OPPHOLDSTILLATELSE",
            sporsmalstekst = "Har du oppholdstillatelse fra utlendingsdirektoratet?",
            svartype = Svartype.JA_NEI,
            svar = listOf(Svar(verdi = "JA")),
            kriterieForVisningAvUndersporsmal = Visningskriterie.JA,
            undersporsmal = listOf(
                Sporsmal(
                    id = UUID.randomUUID().toString(),
                    tag = "MEDLEMSKAP_OPPHOLDSTILLATELSE_VEDTAKSDATO",
                    sporsmalstekst = "Når fikk du vedtak om oppholdstillatelse?",
                    svartype = Svartype.DATO,
                    min = "2013-10-09",
                    max = "2023-10-09",
                    svar = listOf(Svar(verdi = "2023-01-01"))
                ),
                Sporsmal(
                    id = UUID.randomUUID().toString(),
                    tag = "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT",
                    sporsmalstekst = "Har du fått permanent oppholdstillatelse?",
                    svartype = Svartype.JA_NEI,
                    svar = listOf(Svar(verdi = "NEI")),
                    kriterieForVisningAvUndersporsmal = Visningskriterie.NEI,
                    undersporsmal = listOf(
                        Sporsmal(
                            id = UUID.randomUUID().toString(),
                            tag = "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERIODE",
                            sporsmalstekst = "Hvilken periode har du fått oppholdstillatelse?",
                            svartype = Svartype.PERIODE,
                            min = "2013-10-09",
                            max = "2033-10-09",
                            svar = listOf(Svar(verdi = "{\"fom\":\"2022-12-13\",\"tom\":\"2023-01-02\"}"))
                        )
                    )
                )
            )
        )
    )
}

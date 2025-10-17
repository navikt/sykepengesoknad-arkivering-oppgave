package no.nav.helse.flex.oppgave

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.helse.flex.*
import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.*
import no.nav.helse.flex.domain.dto.Avsendertype.BRUKER
import no.nav.helse.flex.domain.dto.Avsendertype.SYSTEM
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class BeskrivelseServiceTest {
    private val objectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .registerModules(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Test
    fun soknadForUtlandsopphold() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadUtland.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_UTLAND)
    }

    @Test
    fun medlemskapssporsmal() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadMedlemskap.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_MEDLEMSKAP)
    }

    @Test
    fun soknadForUtlandsoppholdMedSvartypeLand() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadUtlandMedSvartypeLand.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_UTLAND_MED_SVARTYPE_LAND)
    }

    @Test
    fun soknadForSelvstendigeMedNeisvar() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadSelvstendigMedNeisvar.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_SOKNAD_SELVSTENDIG_MED_NEI_SVAR)
    }

    @Test
    fun soknadForSelvstendigeMedMangeSvar() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadSelvstendigMangeSvar.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_SELVSTENDIG_MANGE_SVAR)
    }

    @Test
    fun soknadForSelvstendigeMedMangeSvarNy() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadSelvstendigMangeSvarNy.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseSelvstendigeMedMangeSvar())
    }

    @Test
    fun soknadForArbeidstakereMedNeisvar() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_ARBEIDSTAKER_MED_NEI_SVAR)
    }

    @Test
    fun soknadForArbeidstakereMedUgyldigTilbakedateringMerknad() {
        val sykepengesoknad =
            plainSøknad()
                .copy(merknaderFraSykmelding = listOf(Merknad(type = "UGYLDIG_TILBAKEDATERING")))
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        val beskrivelseArbeidstakerMedNeisvar =
            """Søknad om sykepenger for perioden 02.01.1970 til 30.08.1973
OBS! Sykmeldingen er avslått grunnet ugyldig tilbakedatering
"""
        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvar)
    }

    @Test
    fun soknadForArbeidstakereMedUgyldigTilbakedateringMerknadOgFlereOpplysninger() {
        val sykepengesoknad =
            plainSøknad()
                .copy(
                    merknaderFraSykmelding =
                        listOf(
                            Merknad(type = "UGYLDIG_TILBAKEDATERING"),
                            Merknad(type = "TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER"),
                        ),
                )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        val beskrivelseArbeidstakerMedNeisvar =
            """Søknad om sykepenger for perioden 02.01.1970 til 30.08.1973
OBS! Sykmeldingen er avslått grunnet ugyldig tilbakedatering
OBS! Tilbakedatert sykmelding er til vurdering
"""
        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvar)
    }

    @Test
    fun `soknad for arbeidstakere med tilbakedatering under behandling merknad`() {
        val sykepengesoknad =
            plainSøknad()
                .copy(
                    merknaderFraSykmelding =
                        listOf(
                            Merknad(type = "UNDER_BEHANDLING"),
                        ),
                )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        val beskrivelseArbeidstakerMedNeisvar =
            """Søknad om sykepenger for perioden 02.01.1970 til 30.08.1973
OBS! Sykmeldingen er tilbakedatert. Tilbakedateringen var ikke behandlet når søknaden ble sendt. Sjekk gosys for resultat på tilbakedatering
"""
        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvar)
    }

    @Test
    fun soknadForArbeidstakereMedUgyldigUkjentMerknadstype() {
        val sykepengesoknad =
            plainSøknad()
                .copy(merknaderFraSykmelding = listOf(Merknad(type = "SVINDEL", beskrivelse = "Farlig")))
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        val beskrivelseArbeidstakerMedNeisvar =
            """Søknad om sykepenger for perioden 02.01.1970 til 30.08.1973
OBS! Sykmeldingen har en merknad Merknad(type=SVINDEL, beskrivelse=Farlig)
"""
        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvar)
    }

    @Test
    fun soknadForArbeidstakereMangeSvar() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadArbeidstakerMangeSvar.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_ARBEIDSTAKER_MANGE_SVAR)
    }

    @Test
    fun korrigertSoknadFremgarAvBeskrivelse() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        soknad.korrigerer = "1234"
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_ARBEIDSTAKER_MED_NEI_SVAR_KORRIGERT)
    }

    @Test
    fun leggerTilMeldingForAvsendertypeSystem() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadArbeidstakerMangeSvar.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad.copy(avsendertype = null), "fnr", "navn")
        val soknadBruker = Soknad.lagSoknad(sykepengesoknad.copy(avsendertype = BRUKER), "fnr", "navn")
        val soknadSystem = Soknad.lagSoknad(sykepengesoknad.copy(avsendertype = SYSTEM), "fnr", "navn")

        assertThat(lagBeskrivelse(soknad)).isEqualTo(BESKRIVELSE_ARBEIDSTAKER_MANGE_SVAR)
        assertThat(lagBeskrivelse(soknadBruker)).isEqualTo(BESKRIVELSE_ARBEIDSTAKER_MANGE_SVAR)
        assertThat(
            lagBeskrivelse(soknadSystem),
        ).isEqualTo("Denne søknaden er autogenerert på grunn av et registrert dødsfall\n" + BESKRIVELSE_ARBEIDSTAKER_MANGE_SVAR)
    }

    @Test
    fun leggerTilMeldingForEgenmeldtSykmelding() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadArbeidstakerMangeSvar.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad.copy(egenmeldtSykmelding = true), "fnr", "navn")

        assertThat(
            lagBeskrivelse(soknad),
        ).isEqualTo("Denne søknaden hører til en egenmeldt sykmelding\n" + BESKRIVELSE_ARBEIDSTAKER_MANGE_SVAR)
    }

    @Test
    fun soknadForBehandlingsdagerMedNeiSvar() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadBehandlingsdagerMedNeisvar.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_BEHANDLINGSDAGER_MED_NEI_SVAR)
    }

    @Test
    fun soknadForBehandlingsdagerMedMangeSvar() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadBehandlingsdagerMedMangeSvar.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_BEHANDLINGSDAGER_MED_MANGE_SVAR)
    }

    @Test
    fun inntektUnderveisIFriskmeldtTilArbeidsformidlingVisesAlltid() {
        val sykepengesoknad =
            objectMapper
                .readValue(
                    BeskrivelseServiceTest::class.java.getResource("/soknadBehandlingsdagerMedMangeSvar.json"),
                    Sykepengesoknad::class.java,
                ).copy(
                    soknadstype = Soknadstype.FRISKMELDT_TIL_ARBEIDSFORMIDLING,
                    sporsmal =
                        listOf(
                            Sporsmal(
                                id = "1",
                                tag = "FTA_INNTEKT_UNDERVEIS",
                                svartype = Svartype.JA_NEI,
                                sporsmalstekst = "Hadde du inntekt i perioden 1. - 1. januar 2020?",
                                svar = listOf(Svar(verdi = "NEI")),
                            ),
                        ),
                )

        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_FRISKMELDT_MED_NEI_INNTEKT)
    }

    @Test
    fun soknadForArbeidstakereMedTimerIkkeCheckedOgProsentChecked() {
        val sykepengesoknad =
            objectMapper.readValue(
                BeskrivelseServiceTest::class.java.getResource("/soknadArbeidstakerMedTimerOgDeretterProsent.json"),
                Sykepengesoknad::class.java,
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(BESKRIVELSE_ARBEIDSTAKER_MED_TIMER_OG_DERETTER_PROSENT)
    }

    @Test
    fun soknadMedInntektsopplysninger() {
        val sykepengesoknad =
            objectMapper
                .readValue(
                    BeskrivelseServiceTest::class.java.getResource("/soknadArbeidstakerMedTimerOgDeretterProsent.json"),
                    Sykepengesoknad::class.java,
                ).copy(
                    sporsmal =
                        listOf(
                            Sporsmal(
                                id = "1",
                                tag = "INNTEKTSOPPLYSNINGER_DRIFT_I_VIRKSOMHETEN",
                                svartype = Svartype.TIMER,
                                sporsmalstekst = "Er det drift?",
                                svar =
                                    listOf(
                                        Svar(
                                            verdi = "100",
                                        ),
                                    ),
                            ),
                        ),
                )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(INNTEKTSOPPLYSNINGER)
    }

    @Test
    fun aarMaanedSvarBlirFormatertMedNorskManedINedtrekk() {
        val sykepengesoknad =
            plainSøknad().copy(
                sporsmal =
                    listOf(
                        Sporsmal(
                            id = "1",
                            tag = "TEST_AAR_MAANED",
                            svartype = Svartype.AAR_MAANED,
                            sporsmalstekst = "Hvilken måned gjelder dette?",
                            svar = listOf(Svar(verdi = "2025-01-15")),
                        ),
                    ),
            )

        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).contains("Hvilken måned gjelder dette?")
        assertThat(beskrivelse).contains("januar 2025")
    }
}

private fun plainSøknad() =
    Sykepengesoknad(
        aktorId = "1",
        fnr = "1",
        id = UUID.randomUUID().toString(),
        opprettet = LocalDateTime.now(),
        soknadstype = Soknadstype.ARBEIDSTAKERE,
        sporsmal = emptyList(),
        status = "SENDT",
        fom = LocalDate.ofEpochDay(1),
        tom = LocalDate.ofEpochDay(1337),
        egenmeldingsdagerFraSykmelding = null,
    )

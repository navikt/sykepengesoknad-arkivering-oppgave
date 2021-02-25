package no.nav.syfo.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.syfo.*
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Avsendertype.BRUKER
import no.nav.syfo.domain.dto.Avsendertype.SYSTEM
import no.nav.syfo.domain.dto.Merknad
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Sykepengesoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class BeskrivelseServiceTest {

    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), KotlinModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Test
    fun soknadForUtlandsopphold() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadUtland.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseUtland)
    }

    @Test
    fun soknadForUtlandsoppholdMedSvartypeLand() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadUtlandMedSvartypeLand.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseUtlandMedSvartypeLand)
    }

    @Test
    fun soknadForSelvstendigeMedNeisvar() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseSoknadSelvstendigMedNeisvar)
    }

    @Test
    fun soknadForSelvstendigeMedMangeSvar() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadSelvstendigMangeSvar.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseSelvstendigMangeSvar)
    }

    @Test
    fun soknadForArbeidstakereMedNeisvar() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvar)
    }

    @Test
    fun soknadForArbeidstakereMedUgyldigTilbakedateringMerknad() {
        val sykepengesoknad = plainSøknad()
            .copy(merknaderFraSykmelding = listOf(Merknad(type = "UGYLDIG_TILBAKEDATERING")))
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        val beskrivelseArbeidstakerMedNeisvar =
            """Søknad om sykepenger for perioden 02.01.1970 - 30.08.1973
OBS! Sykmeldingen er avslått grunnet ugyldig tilbakedatering
"""
        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvar)
    }

    @Test
    fun soknadForArbeidstakereMedUgyldigTilbakedateringMerknadOgFlereOpplysninger() {
        val sykepengesoknad = plainSøknad()
            .copy(
                merknaderFraSykmelding = listOf(
                    Merknad(type = "UGYLDIG_TILBAKEDATERING"),
                    Merknad(type = "TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER")
                )
            )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        val beskrivelseArbeidstakerMedNeisvar =
            """Søknad om sykepenger for perioden 02.01.1970 - 30.08.1973
OBS! Sykmeldingen er avslått grunnet ugyldig tilbakedatering
OBS! Sykmeldingen er avslått grunnet tilbakedatering som krever flere opplysninger
"""
        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvar)
    }

    @Test
    fun soknadForArbeidstakereMedUgyldigUkjentMerknadstype() {
        val sykepengesoknad = plainSøknad()
            .copy(merknaderFraSykmelding = listOf(Merknad(type = "SVINDEL", beskrivelse = "Farlig")))
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        val beskrivelseArbeidstakerMedNeisvar =
            """Søknad om sykepenger for perioden 02.01.1970 - 30.08.1973
OBS! Sykmeldingen har en merknad Merknad(type=SVINDEL, beskrivelse=Farlig)
"""
        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvar)
    }

    @Test
    fun soknadForArbeidstakereMangeSvar() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadArbeidstakerMangeSvar.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMangeSvar)
    }

    @Test
    fun korrigertSoknadFremgarAvBeskrivelse() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        soknad.korrigerer = "1234"
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvarKorrigert)
    }

    @Test
    fun talerAtArbeidssituasjonIkkeErSatt() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn").copy(
            arbeidssituasjon = null,
            soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE
        )
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isNotEmpty()
    }

    @Test
    fun leggerTilMeldingForAvsendertypeSystem() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadArbeidstakerMangeSvar.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad.copy(avsendertype = null), "fnr", "navn")
        val soknadBruker = Soknad.lagSoknad(sykepengesoknad.copy(avsendertype = BRUKER), "fnr", "navn")
        val soknadSystem = Soknad.lagSoknad(sykepengesoknad.copy(avsendertype = SYSTEM), "fnr", "navn")

        assertThat(lagBeskrivelse(soknad)).isEqualTo(beskrivelseArbeidstakerMangeSvar)
        assertThat(lagBeskrivelse(soknadBruker)).isEqualTo(beskrivelseArbeidstakerMangeSvar)
        assertThat(lagBeskrivelse(soknadSystem)).isEqualTo("Denne søknaden er autogenerert på grunn av et registrert dødsfall\n" + beskrivelseArbeidstakerMangeSvar)
    }

    @Test
    fun leggerTilMeldingForEgenmeldtSykmelding() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadArbeidstakerMangeSvar.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad.copy(egenmeldtSykmelding = true), "fnr", "navn")

        assertThat(lagBeskrivelse(soknad)).isEqualTo("Denne søknaden hører til en egenmeldt sykmelding\n" + beskrivelseArbeidstakerMangeSvar)
    }

    @Test
    fun soknadForBehandlingsdagerMedNeiSvar() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadBehandlingsdagerMedNeisvar.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseBehandlingsdagerMedNeisvar)
    }

    @Test
    fun soknadForBehandlingsdagerMedMangeSvar() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadBehandlingsdagerMedMangeSvar.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseBehandlingsdagerMedMangeSvar)
    }

    @Test
    fun soknadForArbeidstakereMedTimerIkkeCheckedOgProsentChecked() {
        val sykepengesoknad = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadArbeidstakerMedTimerOgDeretterProsent.json"),
            Sykepengesoknad::class.java
        )
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedTimerOgDeretterProsent)
    }
}

private fun plainSøknad() = Sykepengesoknad(
    aktorId = "1",
    id = UUID.randomUUID().toString(),
    opprettet = LocalDateTime.now(),
    soknadstype = Soknadstype.ARBEIDSTAKERE,
    sporsmal = emptyList(),
    status = "SENDT",
    fom = LocalDate.ofEpochDay(1),
    tom = LocalDate.ofEpochDay(1337),
)

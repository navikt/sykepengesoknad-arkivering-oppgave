package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.syfo.TestApplication
import no.nav.syfo.beskrivelseArbeidstakerMangeSvar
import no.nav.syfo.beskrivelseArbeidstakerMedNeisvar
import no.nav.syfo.beskrivelseArbeidstakerMedNeisvarKorrigert
import no.nav.syfo.beskrivelseSelvstendigMangeSvar
import no.nav.syfo.beskrivelseSoknadSelvstendigMedNeisvar
import no.nav.syfo.beskrivelseUtland
import no.nav.syfo.beskrivelseUtlandMedSvartypeLand
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Sykepengesoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BeskrivelseServiceTest {

    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), KotlinModule())

    @Test
    fun soknadForUtlandsopphold() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadUtland.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseUtland)
    }

    @Test
    fun soknadForUtlandsoppholdMedSvartypeLand() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadUtlandMedSvartypeLand.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseUtlandMedSvartypeLand)
    }

    @Test
    fun soknadForSelvstendigeMedNeisvar() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseSoknadSelvstendigMedNeisvar)
    }

    @Test
    fun soknadForSelvstendigeMedMangeSvar() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMangeSvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseSelvstendigMangeSvar)
    }

    @Test
    fun soknadForArbeidstakereMedNeisvar() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvar)
    }

    @Test
    fun soknadForArbeidstakereMangeSvar() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMangeSvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMangeSvar)
    }

    @Test
    fun korrigertSoknadFremgarAvBeskrivelse() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        soknad.korrigerer = "1234"
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvarKorrigert)
    }

    @Test
    fun talerAtArbeidssituasjonIkkeErSatt() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn").copy(
            arbeidssituasjon = null,
            soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE
        )
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isNotEmpty()
    }
}

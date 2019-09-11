package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.syfo.*
import no.nav.syfo.TestApplication
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Sykepengesoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.IOException

class BeskrivelseServiceTest {

    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), KotlinModule())

    @Test
    @Throws(IOException::class)
    fun soknadForUtlandsopphold() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadUtland.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseUtland)
    }

    @Test
    @Throws(IOException::class)
    fun soknadForUtlandsoppholdMedSvartypeLand() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadUtlandMedSvartypeLand.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseUtlandMedSvartypeLand)
    }

    @Test
    @Throws(IOException::class)
    fun soknadForSelvstendigeMedNeisvar() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseSoknadSelvstendigMedNeisvar)
    }

    @Test
    @Throws(IOException::class)
    fun soknadForSelvstendigeMedMangeSvar() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMangeSvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseSelvstendigMangeSvar)
    }

    @Test
    @Throws(IOException::class)
    fun soknadForArbeidstakereMedNeisvar() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvar)
    }

    @Test
    @Throws(IOException::class)
    fun soknadForArbeidstakereMangeSvar() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMangeSvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMangeSvar)
    }

    @Test
    @Throws(IOException::class)
    fun korrigertSoknadFremgarAvBeskrivelse() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        soknad.korrigerer = "1234"
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvarKorrigert)
    }

    @Test
    @Throws(IOException::class)
    fun talerAtArbeidssituasjonIkkeErSatt() {
        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")
        soknad.arbeidssituasjon = null
        soknad.soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE
        val beskrivelse = lagBeskrivelse(soknad)

        assertThat(beskrivelse).isNotEmpty()
    }
}

package no.nav.syfo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import org.junit.Test;

import java.io.IOException;

import static no.nav.syfo.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BeskrivelseServiceTest {

    private ObjectMapper objectMapper = new ObjectMapper().registerModules(new JavaTimeModule(), new KotlinModule());

    @Test
    public void soknadForUtlandsopphold() throws IOException {
        Sykepengesoknad sykepengesoknad = objectMapper.readValue(soknadUtland, Sykepengesoknad.class);
        Soknad soknad = Soknad.Companion.lagSoknad(sykepengesoknad, "fnr", "navn");

        String beskrivelse = BeskrivelseService.lagBeskrivelse(soknad);

        assertThat(beskrivelse).isEqualTo(beskrivelseUtland);
    }

    @Test
    public void soknadForUtlandsoppholdMedSvartypeLand() throws IOException {
        Sykepengesoknad sykepengesoknad = objectMapper.readValue(soknadUtlandMedSvartypeLand, Sykepengesoknad.class);
        Soknad soknad = Soknad.Companion.lagSoknad(sykepengesoknad, "fnr", "navn");

        String beskrivelse = BeskrivelseService.lagBeskrivelse(soknad);

        assertThat(beskrivelse).isEqualTo(beskrivelseUtlandMedSvartypeLand);
    }

    @Test
    public void soknadForSelvstendigeMedNeisvar() throws IOException {
        Sykepengesoknad sykepengesoknad = objectMapper.readValue(soknadSelvstendigMedNeisvar, Sykepengesoknad.class);
        Soknad soknad = Soknad.Companion.lagSoknad(sykepengesoknad, "fnr", "navn");

        String beskrivelse = BeskrivelseService.lagBeskrivelse(soknad);

        assertThat(beskrivelse).isEqualTo(beskrivelseSoknadSelvstendigMedNeisvar);
    }

    @Test
    public void soknadForSelvstendigeMedMangeSvar() throws IOException {
        Sykepengesoknad sykepengesoknad = objectMapper.readValue(soknadSelvstendigMangeSvar, Sykepengesoknad.class);
        Soknad soknad = Soknad.Companion.lagSoknad(sykepengesoknad, "fnr", "navn");

        String beskrivelse = BeskrivelseService.lagBeskrivelse(soknad);

        assertThat(beskrivelse).isEqualTo(beskrivelseSelvstendigMangeSvar);
    }

    @Test
    public void soknadForArbeidstakereMedNeisvar() throws IOException {
        Sykepengesoknad sykepengesoknad = objectMapper.readValue(soknadArbeidstakerMedNeisvar, Sykepengesoknad.class);
        Soknad soknad = Soknad.Companion.lagSoknad(sykepengesoknad, "fnr", "navn");

        String beskrivelse = BeskrivelseService.lagBeskrivelse(soknad);

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvar);
    }

    @Test
    public void soknadForArbeidstakereMangeSvar() throws IOException {
        Sykepengesoknad sykepengesoknad = objectMapper.readValue(soknadArbeidstakerMangeSvar, Sykepengesoknad.class);
        Soknad soknad = Soknad.Companion.lagSoknad(sykepengesoknad, "fnr", "navn");

        String beskrivelse = BeskrivelseService.lagBeskrivelse(soknad);

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMangeSvar);
    }

    @Test
    public void korrigertSoknadFremgarAvBeskrivelse() throws IOException {
        Sykepengesoknad sykepengesoknad = objectMapper.readValue(soknadArbeidstakerMedNeisvar, Sykepengesoknad.class);
        Soknad soknad = Soknad.Companion.lagSoknad(sykepengesoknad, "fnr", "navn");
        soknad.setKorrigerer("1234");

        String beskrivelse = BeskrivelseService.lagBeskrivelse(soknad);

        assertThat(beskrivelse).isEqualTo(beskrivelseArbeidstakerMedNeisvarKorrigert);
    }
}

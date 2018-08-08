package no.nav.syfo.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class DeserialiseringTest {
    @Test
    public void deserialiserSoknad() throws IOException {
        String serialisertSoknad = "{\"id\":\"test-kafka-sykepengesoknad\",\"aktorId\":\"aktorId\",\"sykmeldingId\":\"sykmelding-id\",\"soknadstype\":\"SELVSTENDIGE_OG_FRILANSERE\",\"status\":\"TIL_SENDING\",\"fom\":[2018,6,6],\"tom\":[2018,7,7],\"opprettetDato\":[2018,6,6],\"sporsmal\":[{\"id\":\"1\",\"tag\":null,\"uuid\":null,\"sporsmalstekst\":\"Dette er et testspørsmål\",\"undertekst\":null,\"svartype\":\"PROSENT\",\"min\":null,\"max\":null,\"kriterieForVisningAvUndersporsmal\":null,\"svar\":[{\"svarverdiType\":null,\"verdi\":\"69\"}],\"undersporsmal\":null}],\"innsendtDato\":[2018,6,20]}";

        Sykepengesoknad sykepengesoknad = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(serialisertSoknad, Sykepengesoknad.class);
        assertThat(sykepengesoknad.getAktorId()).isEqualTo("aktorId");
        assertThat(sykepengesoknad.getSporsmal().size()).isEqualTo(1);
    }

}

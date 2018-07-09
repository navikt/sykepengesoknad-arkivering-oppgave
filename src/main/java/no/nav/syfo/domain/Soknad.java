package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import no.nav.syfo.domain.dto.Sykepengesoknad;

import java.time.LocalDate;

@Data
@Builder
@Getter
public class Soknad {
    public String aktørId;
    public String soknadsId;
    public LocalDate fom;
    public LocalDate tom;

    public String lagBeskrivelse() {
        return "Beskivelse er ikke implementert enda, se PDF";
    }

    public static Soknad lagSoknad(Sykepengesoknad sykepengesoknad) {
        return Soknad.builder()
                .aktørId("hvor kommer denne fra?")
                .soknadsId(sykepengesoknad.getId())
                .fom(LocalDate.parse(sykepengesoknad.getFom()))
                .tom(LocalDate.parse(sykepengesoknad.getTom()))
                .build();
    }
}

package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import no.nav.syfo.domain.dto.SykepengesoknadDTO;

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

    public static Soknad lagSoknad(SykepengesoknadDTO sykepengesoknadDTO) {
        return Soknad.builder()
                .aktørId("hvor kommer denne fra?")
                .soknadsId(sykepengesoknadDTO.getId())
                .fom(LocalDate.parse(sykepengesoknadDTO.getFom()))
                .tom(LocalDate.parse(sykepengesoknadDTO.getTom()))
                .build();
    }
}

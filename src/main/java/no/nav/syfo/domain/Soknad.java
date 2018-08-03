package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.dto.Sporsmal;
import no.nav.syfo.domain.dto.Sykepengesoknad;

import java.time.LocalDate;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@Data
@Builder
@Getter
@Slf4j
public class Soknad {
    public String aktørId;
    public String soknadsId;
    public LocalDate fom;
    public LocalDate tom;
    public String fnr;
    public String navn;
    public String sendt;
    public List<Sporsmal> sporsmal;

    public String lagBeskrivelse() {
        return "Beskivelse er ikke implementert enda, se PDF";
    }

    public static Soknad lagSoknad(Sykepengesoknad sykepengesoknad) {
        log.info("Lager Soknad med id: {}", sykepengesoknad.getId());
        // TODO: Legg til fnr, navn og sendt i builderen
        return Soknad.builder()
                .aktørId(sykepengesoknad.getAktorId())
                .soknadsId(sykepengesoknad.getId())
                .fom(LocalDate.parse(sykepengesoknad.getFom(), ISO_DATE_TIME))
                .tom(LocalDate.parse(sykepengesoknad.getTom(), ISO_DATE_TIME))
                .sporsmal(sykepengesoknad.getSporsmal())
                .build();
    }
}

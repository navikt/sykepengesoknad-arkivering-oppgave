package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.ws.AktorConsumer;
import no.nav.syfo.consumer.ws.PersonConsumer;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.syfo.domain.dto.Sporsmal;
import no.nav.syfo.domain.dto.Sykepengesoknad;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Getter
@Slf4j
public class Soknad {
    public String aktorId;
    public String soknadsId;
    public Soknadstype soknadstype;
    public LocalDate fom;
    public LocalDate tom;
    public String fnr;
    public String navn;
    public LocalDate innsendtDato;
    public List<Sporsmal> sporsmal;

    private AktorConsumer aktorConsumer;
    private PersonConsumer personConsumer;

    public String lagBeskrivelse() {
        return "Beskivelse er ikke implementert enda, se PDF";
    }

    public static Soknad lagSoknad(Sykepengesoknad sykepengesoknad, String fnr, String navn) {
        return Soknad.builder()
                .aktorId(sykepengesoknad.getAktorId())
                .soknadsId(sykepengesoknad.getId())
                .soknadstype(sykepengesoknad.getSoknadstype())
                .fom(sykepengesoknad.getFom())
                .tom(sykepengesoknad.getTom())
                .innsendtDato(sykepengesoknad.getInnsendtDato())
                .sporsmal(sykepengesoknad.getSporsmal())
                .fnr(fnr)
                .navn(navn)
                .build();
    }
}

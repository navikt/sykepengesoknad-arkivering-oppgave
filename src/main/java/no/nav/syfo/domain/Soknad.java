package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.syfo.domain.dto.Sporsmal;
import no.nav.syfo.domain.dto.Sykepengesoknad;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Getter
public class Soknad {
    String aktorId;
    String soknadsId;
    Soknadstype soknadstype;
    LocalDate fom;
    LocalDate tom;
    String fnr;
    String navn;
    LocalDate innsendtDato;
    List<Sporsmal> sporsmal;
    String korrigerer;
    String korrigertAv;

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
                .korrigerer(sykepengesoknad.getKorrigerer())
                .korrigertAv(sykepengesoknad.getKorrigertAv())
                .build();
    }
}

package no.nav.syfo.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class Sporsmal {

    String id;
    String tag;
    String sporsmalstekst;
    String undertekst;
    Svartype svartype;
    String min;
    String max;
    Visningskriterie kriterieForVisningAvUndersporsmal;
    List<Svar> svar;
    List<Sporsmal> undersporsmal;
}

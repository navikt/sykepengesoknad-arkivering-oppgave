package no.nav.syfo.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Sporsmal {

    String id;
    String uuid;
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

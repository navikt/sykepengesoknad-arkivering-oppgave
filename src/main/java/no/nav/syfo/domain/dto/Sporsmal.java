package no.nav.syfo.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@Data
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

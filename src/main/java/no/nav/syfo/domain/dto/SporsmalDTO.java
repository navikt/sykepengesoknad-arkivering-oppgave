package no.nav.syfo.domain.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SporsmalDTO {

    String id;
    String tag;
    String sporsmalstekst;
    String undertekst;
    SvartypeDTO svartypeDTO;
    String min;
    String max;
    VisningskriterieDTO kriterieForVisningAvUndersporsmal;
    List<SvarDTO> svarDTO;
    List<SporsmalDTO> undersporsmal;
}

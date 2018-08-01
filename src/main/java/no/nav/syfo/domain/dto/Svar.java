package no.nav.syfo.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@Builder
public class Svar {

    SvarverdiType svarverdiType;
    String verdi;

}

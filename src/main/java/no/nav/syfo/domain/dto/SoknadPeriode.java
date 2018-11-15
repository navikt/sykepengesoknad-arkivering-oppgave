package no.nav.syfo.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
@Getter
public class SoknadPeriode {

    LocalDate fom;
    LocalDate tom;
    Integer grad;
}

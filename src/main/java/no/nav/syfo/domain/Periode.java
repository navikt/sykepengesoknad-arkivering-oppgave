package no.nav.syfo.domain;

import lombok.Value;

import java.time.LocalDate;

@Value
public class Periode {
    LocalDate fom;
    LocalDate tom;
}

package no.nav.syfo.domain.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class Sykepengesoknad {

    String id;
    String sykmeldingId;
    String aktorId;
    Soknadstype soknadstype;
    String status;
    LocalDate fom;
    LocalDate tom;
    LocalDate opprettetDato;
    LocalDate innsendtDato;
    List<Sporsmal> sporsmal;
}

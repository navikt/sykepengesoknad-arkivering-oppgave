package no.nav.syfo.domain.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class Sykepengesoknad {

    String id;
    String sykmeldingId;
    String aktorId;
    Soknadstype soknadstype;
    String status;
    String fom;
    String tom;
    String opprettetDato;
    String innsendtDato;
    List<Sporsmal> sporsmal;
}

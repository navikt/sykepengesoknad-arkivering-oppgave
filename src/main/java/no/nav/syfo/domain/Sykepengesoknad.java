package no.nav.syfo.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class Sykepengesoknad {

    String id;
    String sykmeldingId;
    Soknadstype soknadstype;
    String status;
    String fom;
    String tom;
    String opprettetDato;
    List<Sporsmal> sporsmal;

}

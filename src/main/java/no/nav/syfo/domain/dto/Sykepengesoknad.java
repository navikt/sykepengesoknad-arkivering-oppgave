package no.nav.syfo.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
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
    String korrigertAv;
    String korrigerer;
}

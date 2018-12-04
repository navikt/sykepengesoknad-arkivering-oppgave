package no.nav.syfo.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder(toBuilder = true)
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
    LocalDate startSykeforlop;
    LocalDate sykmeldingUtskrevet;
    String arbeidsgiver;
    String korrigerer;
    String korrigertAv;
    Arbeidssituasjon arbeidssituasjon;
    List<SoknadPeriode> soknadPerioder;
    List<Sporsmal> sporsmal;

}

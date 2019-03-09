package no.nav.syfo.domain.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class Sykepengesoknad {

    public String id;
    public String sykmeldingId;
    public String aktorId;
    public Soknadstype soknadstype;
    public String status;
    public LocalDate fom;
    public LocalDate tom;
    public LocalDateTime opprettet;
    public LocalDateTime sendtNav;
    public LocalDateTime sendtArbeidsgiver;
    public LocalDate startSykeforlop;
    public LocalDateTime sykmeldingSkrevet;
    public String arbeidsgiver;
    public String korrigerer;
    public String korrigertAv;
    public Arbeidssituasjon arbeidssituasjon;
    public List<SoknadPeriode> soknadPerioder;
    public List<Sporsmal> sporsmal;

}

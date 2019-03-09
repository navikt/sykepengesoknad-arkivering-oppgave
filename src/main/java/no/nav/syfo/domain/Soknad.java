package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import no.nav.syfo.domain.dto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.Optional.ofNullable;

@Data
@Builder
@Getter
public class Soknad {

    public String aktorId;
    public String soknadsId;
    public String fnr;
    public String navn;
    public Soknadstype soknadstype;
    public LocalDate fom;
    public LocalDate tom;
    public LocalDate innsendtDato;
    public LocalDate sendtArbeidsgiver;
    public LocalDate startSykeforlop;
    public LocalDate sykmeldingUtskrevet;
    public String arbeidsgiver;
    public String korrigerer;
    public String korrigertAv;
    public Arbeidssituasjon arbeidssituasjon;
    public List<SoknadPeriode> soknadPerioder;
    public List<Sporsmal> sporsmal;

    public static Soknad lagSoknad(Sykepengesoknad sykepengesoknad, String fnr, String navn) {
        return Soknad.builder()
                .aktorId(sykepengesoknad.getAktorId())
                .soknadsId(sykepengesoknad.getId())
                .fnr(fnr)
                .navn(navn)
                .soknadstype(sykepengesoknad.getSoknadstype())
                .fom(sykepengesoknad.getFom())
                .tom(sykepengesoknad.getTom())
                .innsendtDato(sykepengesoknad.getSendtNav().toLocalDate())
                .sendtArbeidsgiver(ofNullable(sykepengesoknad.getSendtArbeidsgiver()).map(LocalDateTime::toLocalDate).orElse(null))
                .startSykeforlop(sykepengesoknad.getStartSykeforlop())
                .sykmeldingUtskrevet(ofNullable(sykepengesoknad.getSykmeldingSkrevet()).map(LocalDateTime::toLocalDate).orElse(null))
                .arbeidsgiver(sykepengesoknad.getArbeidsgiver())
                .korrigerer(sykepengesoknad.getKorrigerer())
                .korrigertAv(sykepengesoknad.getKorrigertAv())
                .arbeidssituasjon(sykepengesoknad.getArbeidssituasjon())
                .soknadPerioder(sykepengesoknad.getSoknadPerioder())
                .sporsmal(endreRekkefolgePaSporsmalForPDF(sykepengesoknad.getSporsmal()))
                .build();
    }

    private static List<Sporsmal> endreRekkefolgePaSporsmalForPDF(final List<Sporsmal> sporsmal) {
        return sporsmal.stream()
                .sorted(comparingInt(Soknad::plasseringSporsmalPDF))
                .collect(Collectors.toList());
    }

    private static int plasseringSporsmalPDF(final Sporsmal sporsmal) {
        switch (sporsmal.getTag()) {
            case "BEKREFT_OPPLYSNINGER":
            case "ANSVARSERKLARING":
                return 1;
            case "VAER_KLAR_OVER_AT":
                return 2;
            default:
                return 0;
        }
    }
}

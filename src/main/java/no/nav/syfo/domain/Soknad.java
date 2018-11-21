package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import no.nav.syfo.domain.dto.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;

@Data
@Builder
@Getter
public class Soknad {
    String aktorId;
    String soknadsId;
    String fnr;
    String navn;
    Soknadstype soknadstype;
    LocalDate fom;
    LocalDate tom;
    LocalDate innsendtDato;
    LocalDate startSykeforlop;
    LocalDate sykmeldingUtskrevet;
    String arbeidsgiver;
    String korrigerer;
    String korrigertAv;
    Arbeidssituasjon arbeidssituasjon;
    List<SoknadPeriode> soknadPerioder;
    List<Sporsmal> sporsmal;

    public static Soknad lagSoknad(Sykepengesoknad sykepengesoknad, String fnr, String navn) {
        return Soknad.builder()
                .aktorId(sykepengesoknad.getAktorId())
                .soknadsId(sykepengesoknad.getId())
                .fnr(fnr)
                .navn(navn)
                .soknadstype(sykepengesoknad.getSoknadstype())
                .fom(sykepengesoknad.getFom())
                .tom(sykepengesoknad.getTom())
                .innsendtDato(sykepengesoknad.getInnsendtDato())
                .startSykeforlop(sykepengesoknad.getStartSykeforlop())
                .sykmeldingUtskrevet(sykepengesoknad.getSykmeldingUtskrevet())
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

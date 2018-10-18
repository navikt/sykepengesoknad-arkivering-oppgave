package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.syfo.domain.dto.Sporsmal;
import no.nav.syfo.domain.dto.Sykepengesoknad;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

@Data
@Builder
@Getter
public class Soknad {
    String aktorId;
    String soknadsId;
    Soknadstype soknadstype;
    LocalDate fom;
    LocalDate tom;
    String fnr;
    String navn;
    LocalDate innsendtDato;
    List<Sporsmal> sporsmal;
    String korrigerer;
    String korrigertAv;

    public static Soknad lagSoknad(Sykepengesoknad sykepengesoknad, String fnr, String navn) {
        return Soknad.builder()
                .aktorId(sykepengesoknad.getAktorId())
                .soknadsId(sykepengesoknad.getId())
                .soknadstype(sykepengesoknad.getSoknadstype())
                .fom(sykepengesoknad.getFom())
                .tom(sykepengesoknad.getTom())
                .innsendtDato(sykepengesoknad.getInnsendtDato())
                .sporsmal(sykepengesoknad.getSporsmal())
                .fnr(fnr)
                .navn(navn)
                .korrigerer(sykepengesoknad.getKorrigerer())
                .korrigertAv(sykepengesoknad.getKorrigertAv())
                .build();
    }

    private List<Sporsmal> alleSporsmalOgUndersporsmal() {
        return flatten(sporsmal)
                .collect(toList());
    }

    public Sporsmal getSporsmalMedTag(final String tag) {
        return alleSporsmalOgUndersporsmal().stream()
                .filter(s -> s.getTag().equals(tag))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Søknaden inneholder ikke spørsmål med tag: " + tag));
    }

    private Stream<Sporsmal> flatten(final List<Sporsmal> nonFlatList) {
        return nonFlatList.stream()
                .flatMap(sporsmal -> concat(Stream.of(sporsmal), flatten(sporsmal.getUndersporsmal())));
    }
}

package no.nav.syfo.kafka.mapper;

import no.nav.syfo.domain.dto.*;
import no.nav.syfo.kafka.sykepengesoknad.dto.SporsmalDTO;
import no.nav.syfo.kafka.sykepengesoknad.dto.SvarDTO;
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO;

import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public final class DtoToSykepengesoknadMapper {

    private static <T extends Enum<T>> T konverter(Class<T> tClass, String name) {
        return ofNullable(name).map(n -> Enum.valueOf(tClass, n)).orElse(null);
    }

    private static Svar konverter(SvarDTO svar) {
        return Svar.builder()
                .verdi(svar.getVerdi())
                .build();
    }

    private static Sporsmal konverter(SporsmalDTO sporsmal) {
        return Sporsmal.builder()
                .id(sporsmal.getId())
                .uuid("uuid")
                .tag(sporsmal.getTag())
                .sporsmalstekst(sporsmal.getSporsmalstekst())
                .undertekst(sporsmal.getUndertekst())
                .svartype(konverter(Svartype.class, sporsmal.getSvartype()))
                .min(sporsmal.getMin())
                .max(sporsmal.getMax())
                .kriterieForVisningAvUndersporsmal(konverter(Visningskriterie.class, sporsmal.getKriterieForVisningAvUndersporsmal()))
                .svar(sporsmal.getSvar().stream()
                        .map(DtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .undersporsmal(sporsmal.getUndersporsmal().stream()
                        .map(DtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .build();
    }

    public static Sykepengesoknad konverter(SykepengesoknadDTO sykepengesoknad) {
        return Sykepengesoknad.builder()
                .id(sykepengesoknad.getId())
                .sykmeldingId(sykepengesoknad.getSykmeldingId())
                .aktorId(sykepengesoknad.getAktorId())
                .soknadstype(konverter(Soknadstype.class, sykepengesoknad.getSoknadstype()))
                .status(sykepengesoknad.getStatus())
                .fom(sykepengesoknad.getFom())
                .tom(sykepengesoknad.getTom())
                .opprettetDato(sykepengesoknad.getOpprettetDato())
                .innsendtDato(sykepengesoknad.getInnsendtDato())
                .sporsmal(sykepengesoknad.getSporsmal().stream()
                        .map(DtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .korrigertAv("empty")
                .korrigerer("empty")
                .build();
    }
}

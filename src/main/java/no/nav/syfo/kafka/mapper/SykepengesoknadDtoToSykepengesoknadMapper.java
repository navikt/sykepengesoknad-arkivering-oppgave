package no.nav.syfo.kafka.mapper;

import no.nav.syfo.domain.dto.*;
import no.nav.syfo.kafka.sykepengesoknad.dto.SoknadsperiodeDTO;
import no.nav.syfo.kafka.sykepengesoknad.dto.SporsmalDTO;
import no.nav.syfo.kafka.sykepengesoknad.dto.SvarDTO;
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public final class SykepengesoknadDtoToSykepengesoknadMapper {

    private static <T extends Enum<T>, U extends Enum<U>> T konverter(Class<T> tClass, U from) {
        return ofNullable(from).map(n -> Enum.valueOf(tClass, n.name())).orElse(null);
    }

    private static Svar konverter(SvarDTO svar) {
        return Svar.builder()
                .verdi(svar.getVerdi())
                .build();
    }

    private static Sporsmal konverter(SporsmalDTO sporsmal) {
        return Sporsmal.builder()
                .id(sporsmal.getId())
                .tag(sporsmal.getTag())
                .sporsmalstekst(sporsmal.getSporsmalstekst())
                .undertekst(sporsmal.getUndertekst())
                .svartype(konverter(Svartype.class, sporsmal.getSvartype()))
                .min(sporsmal.getMin())
                .max(sporsmal.getMax())
                .kriterieForVisningAvUndersporsmal(konverter(Visningskriterie.class, sporsmal.getKriteriumForVisningAvUndersporsmal()))
                .svar(stream(sporsmal.getSvar())
                        .map(SykepengesoknadDtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .undersporsmal(stream(sporsmal.getUndersporsmal())
                        .map(SykepengesoknadDtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .build();
    }

    private static SoknadPeriode konverter(SoknadsperiodeDTO soknadPeriode) {
        return SoknadPeriode.builder()
                .fom(soknadPeriode.getFom())
                .tom(soknadPeriode.getTom())
                .grad(soknadPeriode.getSykmeldingsgrad())
                .build();
    }

    public static Sykepengesoknad konverter(SykepengesoknadDTO sykepengesoknad) {
        return Sykepengesoknad.builder()
                .id(sykepengesoknad.getId())
                .sykmeldingId(sykepengesoknad.getSykmeldingId())
                .aktorId(sykepengesoknad.getAktorId())
                .soknadstype(konverter(Soknadstype.class, sykepengesoknad.getType()))
                .status(sykepengesoknad.getStatus().name())
                .fom(sykepengesoknad.getFom())
                .tom(sykepengesoknad.getTom())
                .opprettet(sykepengesoknad.getOpprettet())
                .sendtNav(sykepengesoknad.getSendtNav())
                .sendtArbeidsgiver(sykepengesoknad.getSendtArbeidsgiver())
                .arbeidsgiver(sykepengesoknad.getArbeidsgiver().getNavn())
                .arbeidssituasjon(konverter(Arbeidssituasjon.class, sykepengesoknad.getArbeidssituasjon()))
                .startSykeforlop(sykepengesoknad.getStartSyketilfelle())
                .sykmeldingSkrevet(sykepengesoknad.getSykmeldingSkrevet())
                .korrigertAv(sykepengesoknad.getKorrigertAv())
                .korrigerer(sykepengesoknad.getKorrigerer())
                .soknadPerioder(stream(sykepengesoknad.getSoknadsperioder())
                        .map(SykepengesoknadDtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .sporsmal(stream(sykepengesoknad.getSporsmal())
                        .map(SykepengesoknadDtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .build();
    }

    private static <T> Stream<T> stream(List<T> list) {
        return list == null ? Stream.empty() : list.stream();
    }
}

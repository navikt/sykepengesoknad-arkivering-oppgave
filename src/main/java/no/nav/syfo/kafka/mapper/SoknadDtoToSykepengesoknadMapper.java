package no.nav.syfo.kafka.mapper;

import no.nav.syfo.domain.dto.*;
import no.nav.syfo.kafka.soknad.dto.SoknadDTO;
import no.nav.syfo.kafka.soknad.dto.SoknadPeriodeDTO;
import no.nav.syfo.kafka.soknad.dto.SporsmalDTO;
import no.nav.syfo.kafka.soknad.dto.SvarDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public final class SoknadDtoToSykepengesoknadMapper {

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
                .tag(sporsmal.getTag())
                .sporsmalstekst(sporsmal.getSporsmalstekst())
                .undertekst(sporsmal.getUndertekst())
                .svartype(konverter(Svartype.class, sporsmal.getSvartype()))
                .min(sporsmal.getMin())
                .max(sporsmal.getMax())
                .kriterieForVisningAvUndersporsmal(konverter(Visningskriterie.class, sporsmal.getKriterieForVisningAvUndersporsmal()))
                .svar(stream(sporsmal.getSvar())
                        .map(SoknadDtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .undersporsmal(stream(sporsmal.getUndersporsmal())
                        .map(SoknadDtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .build();
    }

    private static SoknadPeriode konverter(SoknadPeriodeDTO soknadPeriode) {
        return SoknadPeriode.builder()
                .fom(soknadPeriode.getFom())
                .tom(soknadPeriode.getTom())
                .grad(soknadPeriode.getGrad())
                .build();
    }

    public static Sykepengesoknad konverter(SoknadDTO soknad) {
        return Sykepengesoknad.builder()
                .id(soknad.getId())
                .sykmeldingId(soknad.getSykmeldingId())
                .aktorId(soknad.getAktorId())
                .soknadstype(konverter(Soknadstype.class, soknad.getSoknadstype()))
                .status(soknad.getStatus())
                .fom(soknad.getFom())
                .tom(soknad.getTom())
                .opprettet(soknad.getOpprettetDato().atStartOfDay())
                .sendtNav(ofNullable(soknad.getInnsendtDato()).map(LocalDate::atStartOfDay).orElse(null))
                .arbeidsgiver(soknad.getArbeidsgiver())
                .arbeidssituasjon(konverter(Arbeidssituasjon.class, soknad.getArbeidssituasjon()))
                .startSykeforlop(soknad.getStartSykeforlop())
                .sykmeldingSkrevet(ofNullable(soknad.getSykmeldingUtskrevet()).map(LocalDate::atStartOfDay).orElse(null))
                .korrigertAv(soknad.getKorrigertAv())
                .korrigerer(soknad.getKorrigerer())
                .soknadPerioder(stream(soknad.getSoknadPerioder())
                        .map(SoknadDtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .sporsmal(stream(soknad.getSporsmal())
                        .map(SoknadDtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .build();
    }

    private static <T> Stream<T> stream(List<T> list) {
        return list == null ? Stream.empty() : list.stream();
    }
}

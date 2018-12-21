package no.nav.syfo.kafka.mapper;

import no.nav.syfo.domain.dto.*;
import no.nav.syfo.kafka.soknad.dto.SoknadPeriodeDTO;
import no.nav.syfo.kafka.soknad.dto.SporsmalDTO;
import no.nav.syfo.kafka.soknad.dto.SvarDTO;
import no.nav.syfo.kafka.soknad.dto.SoknadDTO;
import no.nav.syfo.kafka.sykepengesoknad.dto.SoknadsperiodeDTO;
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO;

import java.util.ArrayList;
import java.util.List;
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

    private static Svar konverter(no.nav.syfo.kafka.sykepengesoknad.dto.SvarDTO svar) {
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
                .svar(sporsmal.getSvar().stream()
                        .map(DtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .undersporsmal(sporsmal.getUndersporsmal().stream()
                        .map(DtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .build();
    }

    private static Sporsmal konverter(no.nav.syfo.kafka.sykepengesoknad.dto.SporsmalDTO sporsmal) {
        return Sporsmal.builder()
                .id(sporsmal.getId())
                .tag(sporsmal.getTag())
                .sporsmalstekst(sporsmal.getSporsmalstekst())
                .undertekst(sporsmal.getUndertekst())
                .svartype(konverter(Svartype.class, sporsmal.getSvartype().toString()))
                .min(sporsmal.getMin())
                .max(sporsmal.getMax())
                .kriterieForVisningAvUndersporsmal(konverter(Visningskriterie.class, sporsmal.getKriteriumForVisningAvUndersporsmal().toString()))
                .svar(sporsmal.getSvar().stream()
                        .map(DtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .undersporsmal(sporsmal.getUndersporsmal().stream()
                        .map(DtoToSykepengesoknadMapper::konverter)
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

    private static SoknadPeriode konverter(no.nav.syfo.kafka.sykepengesoknad.dto.SoknadsperiodeDTO soknadPeriode) {
        return SoknadPeriode.builder()
                .fom(soknadPeriode.getFom())
                .tom(soknadPeriode.getTom())
                .grad(soknadPeriode.getSykmeldingsgrad())
                .build();
    }

    public static Sykepengesoknad konverter(SoknadDTO sykepengesoknad) {
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
                .arbeidsgiver(sykepengesoknad.getArbeidsgiver())
                .arbeidssituasjon(konverter(Arbeidssituasjon.class, sykepengesoknad.getArbeidssituasjon()))
                .startSykeforlop(sykepengesoknad.getStartSykeforlop())
                .sykmeldingUtskrevet(sykepengesoknad.getSykmeldingUtskrevet())
                .korrigertAv(sykepengesoknad.getKorrigertAv())
                .korrigerer(sykepengesoknad.getKorrigerer())
                .soknadPerioder(sykepengesoknad.getSoknadPerioder() == null
                        ? new ArrayList<>()
                        : sykepengesoknad.getSoknadPerioder().stream()
                        .map(DtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .sporsmal(sykepengesoknad.getSporsmal().stream()
                        .map(DtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .build();
    }


    public static Sykepengesoknad konverter(SykepengesoknadDTO sykepengesoknad) {
        return Sykepengesoknad.builder()
                .id(sykepengesoknad.getId())
                .sykmeldingId(sykepengesoknad.getSykmeldingId())
                .aktorId(sykepengesoknad.getAktorId())
                .soknadstype(konverter(Soknadstype.class, sykepengesoknad.getType().toString()))
                .status(sykepengesoknad.getStatus().toString())
                .fom(sykepengesoknad.getFom())
                .tom(sykepengesoknad.getTom())
                .opprettetDato(sykepengesoknad.getOpprettet().toLocalDate())
                .innsendtDato(sykepengesoknad.getSendtNav().toLocalDate())
                .arbeidsgiver(sykepengesoknad.getArbeidsgiver().getNavn())
                .arbeidssituasjon(konverter(Arbeidssituasjon.class, sykepengesoknad.getArbeidssituasjon().toString()))
                .startSykeforlop(sykepengesoknad.getStartSyketilfelle())
                .sykmeldingUtskrevet(sykepengesoknad.getSykmeldingSkrevet().toLocalDate())
                .korrigertAv(sykepengesoknad.getKorrigertAv())
                .korrigerer(sykepengesoknad.getKorrigerer())
                .soknadPerioder(sykepengesoknad.getSoknadsperioder() == null
                        ? new ArrayList<>()
                        : sykepengesoknad.getSoknadsperioder().stream()
                        .map(DtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .sporsmal(sykepengesoknad.getSporsmal().stream()
                        .map(DtoToSykepengesoknadMapper::konverter)
                        .collect(Collectors.toList()))
                .build();
    }

}

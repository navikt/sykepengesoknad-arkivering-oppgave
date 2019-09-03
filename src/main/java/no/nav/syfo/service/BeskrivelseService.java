package no.nav.syfo.service;

import no.nav.syfo.domain.Periode;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.SoknadPeriode;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.syfo.domain.dto.Sporsmal;
import no.nav.syfo.domain.dto.Svar;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Optional.empty;
import static no.nav.syfo.domain.dto.Svartype.*;
import static no.nav.syfo.util.DatoUtil.norskDato;
import static no.nav.syfo.util.PeriodeMapper.jsonTilPeriode;

public class BeskrivelseService {

    public static String lagBeskrivelse(final Soknad soknad) {
        String tittel;
        switch (soknad.getSoknadstype()) {
            case ARBEIDSTAKERE:
                tittel = "Søknad om sykepenger for perioden " +
                        soknad.getFom().format(norskDato) + " - " + soknad.getTom().format(norskDato);
                break;
            case SELVSTENDIGE_OG_FRILANSERE:
                // Det kan finnes eldre søknader som mangler arbeidssituasjon
                String arbeidssituasjon = soknad.getArbeidssituasjon() != null ? soknad.getArbeidssituasjon().getNavn() : "Selvstendig Næringsdrivende / Frilanser";
                tittel = "Søknad om sykepenger fra " + arbeidssituasjon + " for perioden " +
                        soknad.getFom().format(norskDato) + " - " + soknad.getTom().format(norskDato);
                break;
            case OPPHOLD_UTLAND:
                tittel = "Søknad om å beholde sykepenger i utlandet";
                break;
            default:
                throw new RuntimeException("Beskrivelse er ikke implementert for søknadstype: " + soknad.getSoknadstype());
        }
        return tittel + (soknad.getKorrigerer() != null ? " KORRIGERING" : "") + "\n" +
                beskrivArbeidsgiver(soknad) +
                Optional.ofNullable(soknad.getSoknadPerioder()).map(BeskrivelseService::beskrivPerioder).orElse("") +
                soknad.getSporsmal().stream()
                        .filter(BeskrivelseService::sporsmalSkalVises)
                        .map(sporsmal -> beskrivSporsmal(sporsmal, 0))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.joining("\n"));
    }

    private static String beskrivPerioder(final List<SoknadPeriode> perioder) {
        return IntStream.range(0, perioder.size())
                .mapToObj(i -> {
                    SoknadPeriode soknadPeriode = perioder.get(i);
                    String faktiskGrad = soknadPeriode.getFaktiskGrad() != null
                            ? "Oppgitt faktisk arbeidsgrad: " + soknadPeriode.getFaktiskGrad() + "\n"
                            : "";
                    return "\nPeriode " + (i + 1) + ":\n" +
                            soknadPeriode.getFom().format(norskDato) + " - " + soknadPeriode.getTom().format(norskDato) + "\n" +
                            "Grad: " + soknadPeriode.getGrad() + "\n" +
                            faktiskGrad;
                })
                .collect(Collectors.joining());

    }

    private static String beskrivArbeidsgiver(final Soknad soknad) {
        return soknad.getSoknadstype() == Soknadstype.ARBEIDSTAKERE
            ?  "\nArbeidsgiver: " + soknad.getArbeidsgiver() + "\n"
            : "";
    }

    private static boolean sporsmalSkalVises(final Sporsmal sporsmal) {
        switch (sporsmal.getTag()) {
            case "ANSVARSERKLARING":
            case "BEKREFT_OPPLYSNINGER":
                return false;
            case "ARBEIDSGIVER":
                return true;
            default:
                return !"NEI".equals(getForsteSvarverdi(sporsmal));
        }
    }

    private static Optional<String> beskrivSporsmal(final Sporsmal sporsmal, final int dybde) {
        final String innrykk = "\n" + String.join("", nCopies(dybde, "    "));
        final List<String> svarverdier = getSvarverdier(sporsmal);
        return svarverdier.isEmpty() && Stream.of(CHECKBOX_GRUPPE, RADIO_GRUPPE, RADIO_GRUPPE_TIMER_PROSENT).noneMatch(svartype -> svartype.equals(sporsmal.getSvartype()))
                ? empty()
                : Optional.of(formatterSporsmalOgSvar(sporsmal).stream()
                .map(sporsmalOgSvar -> innrykk + sporsmalOgSvar)
                .collect(Collectors.joining()) +
                getUndersporsmalIgnorerRadioIGruppeTimerProsent(sporsmal).stream()
                        .map(undersporsmal -> beskrivSporsmal(undersporsmal, getNesteDybde(sporsmal, dybde)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.joining("\n")));
    }

    private static int getNesteDybde(final Sporsmal sporsmal, final int dybde) {
        return Stream.of(RADIO_GRUPPE, RADIO_GRUPPE_TIMER_PROSENT).anyMatch(svartype -> svartype.equals(sporsmal.getSvartype()))
                ? dybde
                : dybde + 1;
    }

    private static List<Sporsmal> getUndersporsmalIgnorerRadioIGruppeTimerProsent(final Sporsmal sporsmal) {
        return RADIO_GRUPPE_TIMER_PROSENT == sporsmal.getSvartype()
                ? sporsmal.getUndersporsmal().stream()
                .map(Sporsmal::getUndersporsmal)
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                : sporsmal.getUndersporsmal();
    }

    private static List<String> formatterSporsmalOgSvar(final Sporsmal sporsmal) {
        switch (sporsmal.getSvartype()) {
            case CHECKBOX:
            case CHECKBOX_GRUPPE:
            case RADIO:
            case RADIO_GRUPPE:
            case RADIO_GRUPPE_TIMER_PROSENT:
                return singletonList(sporsmal.getSporsmalstekst());
            case JA_NEI:
                return asList(sporsmal.getSporsmalstekst(), "JA".equals(getForsteSvarverdi(sporsmal)) ? "Ja" : "Nei");
            case DATO:
                return asList(sporsmal.getSporsmalstekst(), formatterDato(getForsteSvarverdi(sporsmal)));
            case PERIODE:
                return asList(sporsmal.getSporsmalstekst(), formatterPeriode(getForsteSvarverdi(sporsmal)));
            case PERIODER:
                return Stream.concat(Stream.of(sporsmal.getSporsmalstekst()),
                        getSvarverdier(sporsmal).stream()
                                .map(BeskrivelseService::formatterPeriode))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            case LAND:
                return Stream.concat(Stream.of(sporsmal.getSporsmalstekst()),
                        getSvarverdier(sporsmal).stream()
                                .map(BeskrivelseService::formatterLand))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            case TALL:
                return Stream.of(sporsmal.getSporsmalstekst(),
                        getForsteSvarverdi(sporsmal) + " " + sporsmal.getUndertekst())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            case TIMER:
                return asList(sporsmal.getSporsmalstekst(), getForsteSvarverdi(sporsmal) + " timer");
            case PROSENT:
                return asList(sporsmal.getSporsmalstekst(), getForsteSvarverdi(sporsmal) + " prosent");
            case FRITEKST:
                return asList(sporsmal.getSporsmalstekst(), getForsteSvarverdi(sporsmal));
            default:
                return emptyList();
        }
    }

    private static List<String> getSvarverdier(final Sporsmal sporsmal) {
        return sporsmal.getSvar().stream()
                .map(Svar::getVerdi)
                .collect(Collectors.toList());
    }

    private static String getForsteSvarverdi(Sporsmal sporsmal) {
        return sporsmal.getSvar().isEmpty()
                ? ""
                : sporsmal.getSvar().get(0).getVerdi();
    }

    private static String formatterDato(final String svarverdi) {
        return LocalDate.parse(svarverdi).format(norskDato);
    }

    private static String formatterPeriode(final String svarverdi) {
        final Periode periode = jsonTilPeriode(svarverdi);
        return periode.getFom().format(norskDato) + " - " +
                periode.getTom().format(norskDato);
    }

    private static String formatterLand(final String svarverdi) {
        return "- " + svarverdi;
    }
}

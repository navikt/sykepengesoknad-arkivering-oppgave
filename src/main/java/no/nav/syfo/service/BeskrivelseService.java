package no.nav.syfo.service;

import no.nav.syfo.domain.Periode;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sporsmal;
import no.nav.syfo.domain.dto.Svar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Optional.empty;
import static no.nav.syfo.domain.dto.Svartype.CHECKBOX_GRUPPE;
import static no.nav.syfo.util.DatoUtil.norskDato;
import static no.nav.syfo.util.PeriodeMapper.jsonTilPeriode;

public class BeskrivelseService {

    public static String lagBeskrivelse(final Soknad soknad) {
        String tittel;
        switch (soknad.getSoknadstype()) {
            case SELVSTENDIGE_OG_FRILANSERE:
                tittel = tittelSelvstendigFrilanser(soknad);
                break;
            case OPPHOLD_UTLAND:
                tittel = "Søknad om å beholde sykepenger i utlandet ";
                break;
            default:
                throw new RuntimeException("Beskrivelse er ikke implementert for søknadstype: " + soknad.getSoknadstype());
        }
        return tittel + "\n" +
                soknad.getSporsmal().stream()
                        .filter(BeskrivelseService::sporsmalSkalVises)
                        .map(sporsmal -> beskrivSporsmal(sporsmal, 0))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.joining("\n"));
    }

    private static String tittelSelvstendigFrilanser(final Soknad soknad) {
        final boolean frilanser = !soknad
                .getSporsmalMedTag("INNTEKTSKILDE_FRILANSER_SELVSTENDIG")
                .getSporsmalstekst()
                .equals("Frilanser");

        return "Søknad om sykepenger for " +
                (frilanser ? "frilanser " : "selvstendig næringsdrivende ") +
                "for perioden " + soknad.getFom().format(norskDato) +
                " - " + soknad.getTom().format(norskDato);
    }

    private static boolean sporsmalSkalVises(final Sporsmal sporsmal) {
        switch (sporsmal.getTag()) {
            case "ANSVARSERKLARING":
            case "BEKREFT_OPPLYSNINGER":
                return false;
            case "ARBEIDSGIVER":
                return true;
            default:
                return !"NEI".equals(getForsteSvarverdi(sporsmal))
                        || sporsmal.getTag().startsWith("JOBBET_DU_GRADERT_")
                        || sporsmal.getTag().startsWith("JOBBET_DU_100_PROSENT_");
        }
    }

    private static Optional<String> beskrivSporsmal(final Sporsmal sporsmal, final int dybde) {
        final String innrykk = "\n" + String.join("", nCopies(dybde, "    "));
        final List<String> svarverdier = getSvarverdier(sporsmal);
        return svarverdier.isEmpty() && !CHECKBOX_GRUPPE.equals(sporsmal.getSvartype())
                ? empty()
                : Optional.of(formatterSporsmalOgSvar(sporsmal).stream()
                .map(sporsmalOgSvar -> innrykk + sporsmalOgSvar)
                .collect(Collectors.joining()) +
                sporsmal.getUndersporsmal().stream()
                        .map(undersporsmal -> beskrivSporsmal(undersporsmal, dybde + 1))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.joining("\n")));
    }

    private static List<String> formatterSporsmalOgSvar(final Sporsmal sporsmal) {
        switch (sporsmal.getSvartype()) {
            case CHECKBOX:
            case CHECKBOX_GRUPPE:
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
}

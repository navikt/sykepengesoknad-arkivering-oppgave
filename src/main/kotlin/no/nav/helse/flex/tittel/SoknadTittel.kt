package no.nav.helse.flex.tittel

import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.util.DatoUtil

fun Soknad.periodeTekst(): String = "for perioden ${fom!!.format(DatoUtil.norskDato)} til ${tom!!.format(DatoUtil.norskDato)}"

fun Soknad.skapTittelForNaringsdrivendeFrilanser(): String {
    val vedlegg =
        if (sporsmal.any { it.tag == "INNTEKTSOPPLYSNINGER_DRIFT_VIRKSOMHETEN" }) {
            " - med vedlegg inntektsopplysninger"
        } else {
            ""
        }
    val arbeidssituasjon = arbeidssituasjon?.toString()?.lowercase() ?: "Selvstendig Næringsdrivende / Frilanser"

    return "Søknad om sykepenger for $arbeidssituasjon ${periodeTekst()}$vedlegg"
}

fun Soknad.skapTittel(): String {
    // Det kan finnes eldre søknader som mangler arbeidssituasjon

    val arbeidssituasjon = arbeidssituasjon?.toString()?.lowercase() ?: "Selvstendig Næringsdrivende / Frilanser"

    return when (soknadstype) {
        Soknadstype.OPPHOLD_UTLAND -> "Søknad om å beholde sykepenger utenfor EØS"
        Soknadstype.SELVSTENDIGE_OG_FRILANSERE -> skapTittelForNaringsdrivendeFrilanser()
        Soknadstype.ARBEIDSTAKERE -> "Søknad om sykepenger ${periodeTekst()}"
        Soknadstype.ARBEIDSLEDIG -> "Søknad om sykepenger for arbeidsledig ${periodeTekst()}"
        Soknadstype.BEHANDLINGSDAGER -> "Søknad om enkeltstående behandlingsdager for $arbeidssituasjon ${periodeTekst()}"
        Soknadstype.ANNET_ARBEIDSFORHOLD -> "Søknad om sykepenger med uavklart arbeidssituasjon fra ${periodeTekst()}"
        Soknadstype.REISETILSKUDD -> "Søknad om reisetilskudd ${periodeTekst()}"
        Soknadstype.GRADERT_REISETILSKUDD -> "Søknad om sykepenger med reisetilskudd ${periodeTekst()}"
    }
}

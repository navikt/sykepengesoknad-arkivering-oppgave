package no.nav.helse.flex.tittel

import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Arbeidssituasjon
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.util.DatoUtil

fun Soknad.periodeTekst(): String = "for perioden ${fom!!.format(DatoUtil.norskDato)} til ${tom!!.format(DatoUtil.norskDato)}"

private fun Soknad.skapTittelForNaringsdrivendeFrilanser(): String {
    val vedlegg =
        if (sporsmal.any { it.tag.contains("INNTEKTSOPPLYSNINGER") }) {
            " - med vedlegg inntektsopplysninger"
        } else {
            ""
        }

    return "Søknad om sykepenger for ${presentabelArbeidssituasjon()} ${periodeTekst()}$vedlegg"
}

private fun Soknad.presentabelArbeidssituasjon(): String? {
    return when (this.arbeidssituasjon) {
        Arbeidssituasjon.NAERINGSDRIVENDE -> "næringsdrivende"
        Arbeidssituasjon.FISKER -> {
            var fiskeTekst = "fisker"
            if (fiskerBlad != null) {
                fiskeTekst += " på blad $fiskerBlad"
            }
            return fiskeTekst
        }
        null -> throw RuntimeException(
            "Arbeidssituasjon er null, dette skal ikke kunne skje på nye data. " +
                "Det er 2 gamle næringsdrivende søknader som mangler arbeidssituasjon",
        )

        else -> this.arbeidssituasjon.toString().lowercase()
    }
}

fun Soknad.skapTittel(): String =
    when (soknadstype) {
        Soknadstype.OPPHOLD_UTLAND -> "Søknad om å beholde sykepenger utenfor EØS"
        Soknadstype.SELVSTENDIGE_OG_FRILANSERE -> skapTittelForNaringsdrivendeFrilanser()
        Soknadstype.ARBEIDSTAKERE -> "Søknad om sykepenger ${periodeTekst()}"
        Soknadstype.ARBEIDSLEDIG -> "Søknad om sykepenger for arbeidsledig ${periodeTekst()}"
        Soknadstype.BEHANDLINGSDAGER -> "Søknad om enkeltstående behandlingsdager for ${presentabelArbeidssituasjon()} ${periodeTekst()}"
        Soknadstype.ANNET_ARBEIDSFORHOLD -> "Søknad om sykepenger med uavklart arbeidssituasjon fra ${periodeTekst()}"
        Soknadstype.REISETILSKUDD -> "Søknad om reisetilskudd ${periodeTekst()}"
        Soknadstype.GRADERT_REISETILSKUDD -> "Søknad om sykepenger med reisetilskudd ${periodeTekst()}"
    }

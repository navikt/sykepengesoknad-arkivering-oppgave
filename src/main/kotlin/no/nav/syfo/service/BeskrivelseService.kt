package no.nav.syfo.service

import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Avsendertype.SYSTEM
import no.nav.syfo.domain.dto.SoknadPeriode
import no.nav.syfo.domain.dto.Soknadstype.ARBEIDSLEDIG
import no.nav.syfo.domain.dto.Soknadstype.ARBEIDSTAKERE
import no.nav.syfo.domain.dto.Soknadstype.OPPHOLD_UTLAND
import no.nav.syfo.domain.dto.Soknadstype.SELVSTENDIGE_OG_FRILANSERE
import no.nav.syfo.domain.dto.Sporsmal
import no.nav.syfo.domain.dto.Svartype.CHECKBOX
import no.nav.syfo.domain.dto.Svartype.CHECKBOX_GRUPPE
import no.nav.syfo.domain.dto.Svartype.DATO
import no.nav.syfo.domain.dto.Svartype.FRITEKST
import no.nav.syfo.domain.dto.Svartype.JA_NEI
import no.nav.syfo.domain.dto.Svartype.LAND
import no.nav.syfo.domain.dto.Svartype.PERIODE
import no.nav.syfo.domain.dto.Svartype.PERIODER
import no.nav.syfo.domain.dto.Svartype.PROSENT
import no.nav.syfo.domain.dto.Svartype.RADIO
import no.nav.syfo.domain.dto.Svartype.RADIO_GRUPPE
import no.nav.syfo.domain.dto.Svartype.RADIO_GRUPPE_TIMER_PROSENT
import no.nav.syfo.domain.dto.Svartype.TALL
import no.nav.syfo.domain.dto.Svartype.TIMER
import no.nav.syfo.util.DatoUtil.norskDato
import no.nav.syfo.util.PeriodeMapper.jsonTilPeriode
import java.time.LocalDate
import java.util.Collections.nCopies

fun lagBeskrivelse(soknad: Soknad): String {
    return soknad.meldingDersomAvsendertypeErSystem() +
            soknad.lagTittel() +
            soknad.erKorrigert() + "\n" +
            soknad.beskrivArbeidsgiver() +
            soknad.beskrivPerioder() +
            soknad.beskrivFaktiskGradFrilansere() +
            soknad.sporsmal
                    .asSequence()
                    .filter { it.skalVises() }
                    .map { sporsmal -> beskrivSporsmal(sporsmal, 0) }
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
}

private fun Soknad.meldingDersomAvsendertypeErSystem() =
        if (avsendertype == SYSTEM)
            "Denne søknaden er autogenerert på grunn av et registrert dødsfall\n"
        else
            ""

private fun Soknad.lagTittel() =
        when (soknadstype) {
            ARBEIDSTAKERE -> "Søknad om sykepenger for perioden ${fom!!.format(norskDato)} - ${tom!!.format(norskDato)}"
            SELVSTENDIGE_OG_FRILANSERE -> {
                // Det kan finnes eldre søknader som mangler arbeidssituasjon
                val arbeidssituasjon = arbeidssituasjon?.navn ?: "Selvstendig Næringsdrivende / Frilanser"
                "Søknad om sykepenger fra $arbeidssituasjon for perioden ${fom!!.format(norskDato)} - ${tom!!.format(norskDato)}"
            }
            OPPHOLD_UTLAND -> "Søknad om å beholde sykepenger utenfor EØS"
            ARBEIDSLEDIG -> "Søknad om sykepenger for arbeidsledig"
            null -> error("Mangler søknadstype for $soknadsId")
        }

private fun Soknad.erKorrigert() =
        korrigerer?.let { " KORRIGERING" } ?: ""

private fun Soknad.beskrivArbeidsgiver() =
        if (soknadstype === ARBEIDSTAKERE)
            "\nArbeidsgiver: $arbeidsgiver\n"
        else
            ""

private fun Soknad.beskrivPerioder() =
        soknadPerioder?.mapIndexed { index, periode ->
            "\nPeriode ${index + 1}:\n" +
                    "${periode.fom!!.format(norskDato)} - ${periode.tom!!.format(norskDato)}\n" +
                    "Grad: ${periode.grad}\n" +
                    (periode.faktiskGrad?.let { faktiskGrad ->
                        "Oppgitt faktisk arbeidsgrad: $faktiskGrad\n"
                    } ?: "")
        }?.joinToString("") ?: ""

// TODO: Tror det er laget en egen beskrivelse for frilanser siden søknaden fungerer annerledes enn arbeidstaker, dette blir kanskje fikset når arbeidstakersøknaden og frilanser oppretter søknadene likt...
private fun Soknad.beskrivFaktiskGradFrilansere(): String {
    if (soknadstype === SELVSTENDIGE_OG_FRILANSERE) {
        val harJobbetMerEnnGradert = sporsmal
                .asSequence()
                .filter { it.tag.startsWith("JOBBET_DU_GRADERT_") || it.tag.startsWith("JOBBET_DU_100_PROSENT_") }
                .any { it.svar?.asSequence()?.any { svar -> svar.verdi == "JA" } ?: false }

        if (harJobbetMerEnnGradert) {
            return """
                
                OBS! Brukeren har jobbet mer enn uføregraden i sykmeldingen.
                Se oppgitt arbeidsgrad lengre ned i oppgaven
                
                """.trimIndent()
        }
    }
    return ""
}

private fun Sporsmal.skalVises() =
        when (tag) {
            "ANSVARSERKLARING", "BEKREFT_OPPLYSNINGER", "EGENMELDINGER" -> false
            "ARBEIDSGIVER" -> true
            else -> "NEI" != forsteSvarverdi()
        }

private fun beskrivSporsmal(sporsmal: Sporsmal, dybde: Int): String {
    val innrykk = "\n" + nCopies(dybde, "    ").joinToString("")
    val svarverdier = sporsmal.svarverdier()

    return if (svarverdier.isEmpty() && sporsmal.svartype !in listOf(CHECKBOX_GRUPPE, RADIO_GRUPPE, RADIO_GRUPPE_TIMER_PROSENT)) {
        ""
    } else {
        sporsmal.formatterSporsmalOgSvar().joinToString("") { sporsmalOgSvar ->
            innrykk + sporsmalOgSvar
        }.plus(
                sporsmal.undersporsmalIgnorerRadioIGruppeTimerProsent()
                        ?.map { beskrivSporsmal(it, getNesteDybde(sporsmal, dybde)) }
                        ?.filter { it.isNotBlank() }
                        ?.joinToString("\n")
                        ?: ""
        )
    }
}

private fun getNesteDybde(sporsmal: Sporsmal, dybde: Int): Int {
    return when (sporsmal.svartype) {
        RADIO_GRUPPE, RADIO_GRUPPE_TIMER_PROSENT -> dybde
        else -> dybde + 1
    }
}

private fun Sporsmal.undersporsmalIgnorerRadioIGruppeTimerProsent(): List<Sporsmal>? {
    return if (RADIO_GRUPPE_TIMER_PROSENT === svartype)
        undersporsmal!!
                .map { it.undersporsmal }
                .flatMap { it!! }
                .toList()
    else
        undersporsmal
}

private fun Sporsmal.formatterSporsmalOgSvar(): List<String> {
    return when (svartype) {
        CHECKBOX, CHECKBOX_GRUPPE, RADIO, RADIO_GRUPPE, RADIO_GRUPPE_TIMER_PROSENT ->
            listOfNotNull(sporsmalstekst)
        JA_NEI -> listOfNotNull(sporsmalstekst, if ("JA" == forsteSvarverdi()) "Ja" else "Nei")
        DATO -> listOfNotNull(sporsmalstekst, formatterDato(forsteSvarverdi()))
        PERIODE -> listOfNotNull(sporsmalstekst, formatterPeriode(forsteSvarverdi()))
        PERIODER -> listOfNotNull(sporsmalstekst) + svarverdier().map { formatterPeriode(it) }
        LAND -> listOfNotNull(sporsmalstekst) + svarverdier().map { formatterLand(it) }
        TALL -> listOfNotNull(sporsmalstekst, forsteSvarverdi() + " " + undertekst)
        TIMER -> listOfNotNull(sporsmalstekst, forsteSvarverdi() + " timer")
        PROSENT -> listOfNotNull(sporsmalstekst, forsteSvarverdi() + " prosent")
        FRITEKST -> listOfNotNull(sporsmalstekst, forsteSvarverdi())
        else -> emptyList()
    }
}

private fun Sporsmal.svarverdier(): List<String> {
    return svar?.mapNotNull { it.verdi } ?: emptyList()
}

private fun Sporsmal.forsteSvarverdi(): String {
    return svar?.firstOrNull()?.verdi ?: ""
}

private fun formatterDato(svarverdi: String?): String {
    return LocalDate.parse(svarverdi!!).format(norskDato)
}

private fun formatterPeriode(svarverdi: String?): String {
    val (fom, tom) = jsonTilPeriode(svarverdi)
    return fom.format(norskDato) + " - " +
            tom.format(norskDato)
}

private fun formatterLand(svarverdi: String): String {
    return "- $svarverdi"
}

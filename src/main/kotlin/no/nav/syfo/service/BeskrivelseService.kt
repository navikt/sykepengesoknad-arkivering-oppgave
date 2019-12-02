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
    val tittel = when (soknad.soknadstype) {
        ARBEIDSTAKERE -> "Søknad om sykepenger for perioden ${soknad.fom!!.format(norskDato)} - ${soknad.tom!!.format(norskDato)}"
        SELVSTENDIGE_OG_FRILANSERE -> {
            // Det kan finnes eldre søknader som mangler arbeidssituasjon
            val arbeidssituasjon = soknad.arbeidssituasjon?.navn ?: "Selvstendig Næringsdrivende / Frilanser"
            "Søknad om sykepenger fra $arbeidssituasjon for perioden ${soknad.fom!!.format(norskDato)} - ${soknad.tom!!.format(norskDato)}"
        }
        OPPHOLD_UTLAND -> "Søknad om å beholde sykepenger utenfor EØS"
        ARBEIDSLEDIG -> "Søknad om sykepenger for arbeidsledig"
        null -> error("Mangler søknadstype for ${soknad.soknadsId}")
    }

    return (if (SYSTEM == soknad.avsendertype) "Denne søknaden er autogenerert på grunn av et registrert dødsfall\n" else "") +
            tittel + (soknad.korrigerer?.let { " KORRIGERING" } ?: "") + "\n" +
            beskrivArbeidsgiver(soknad) +
            (soknad.soknadPerioder?.let { beskrivPerioder(it) } ?: "") +
            beskrivFaktiskGradFrilansere(soknad) +

            soknad.sporsmal
                    .asSequence()
                    .filter { sporsmalSkalVises(it) }
                    .map { sporsmal -> beskrivSporsmal(sporsmal, 0) }
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
}

private fun beskrivPerioder(perioder: List<SoknadPeriode>): String {
    return perioder.mapIndexed(){ index, soknadPeriode ->
        val (fom, tom, grad, faktiskGrad) = soknadPeriode
        "\nPeriode " + (index + 1) + ":\n" +
                fom!!.format(norskDato) + " - " + tom!!.format(norskDato) + "\n" +
                "Grad: " + grad + "\n" +
                (faktiskGrad?.let { "Oppgitt faktisk arbeidsgrad: $it\n" } ?: "")
        }.joinToString("")
}

private fun beskrivFaktiskGradFrilansere(soknad: Soknad): String {
    if (soknad.soknadstype === SELVSTENDIGE_OG_FRILANSERE) {
        val harJobbetMerEnnGradert = soknad.sporsmal.asSequence()
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

private fun beskrivArbeidsgiver(soknad: Soknad): String {
    return  if (soknad.soknadstype === ARBEIDSTAKERE)
                "\nArbeidsgiver: " + soknad.arbeidsgiver + "\n"
            else
                ""
}

private fun sporsmalSkalVises(sporsmal: Sporsmal): Boolean {
    return when (sporsmal.tag) {
        "ANSVARSERKLARING", "BEKREFT_OPPLYSNINGER", "EGENMELDINGER" -> false
        "ARBEIDSGIVER" -> true
        else -> "NEI" != getForsteSvarverdi(sporsmal)
    }
}

private fun beskrivSporsmal(sporsmal: Sporsmal, dybde: Int): String {
    val innrykk = "\n" + nCopies(dybde, "    ").joinToString("")
    val svarverdier = getSvarverdier(sporsmal)

    return if (svarverdier.isEmpty() && sporsmal.svartype !in listOf(CHECKBOX_GRUPPE, RADIO_GRUPPE, RADIO_GRUPPE_TIMER_PROSENT)) {
        ""
    }
    else {
        formatterSporsmalOgSvar(sporsmal).joinToString("") {
                sporsmalOgSvar -> innrykk + sporsmalOgSvar
            }.plus (
                getUndersporsmalIgnorerRadioIGruppeTimerProsent(sporsmal)
                        ?.map { beskrivSporsmal(it, getNesteDybde(sporsmal, dybde)) }
                        ?.filter { it.isNotBlank() }
                        ?.joinToString("\n")
                        ?: ""
            )
    }
}

private fun getNesteDybde(sporsmal: Sporsmal, dybde: Int): Int {
    return when (sporsmal.svartype) {
        RADIO_GRUPPE,RADIO_GRUPPE_TIMER_PROSENT -> dybde
        else -> dybde + 1
    }
}

private fun getUndersporsmalIgnorerRadioIGruppeTimerProsent(sporsmal: Sporsmal): List<Sporsmal>? {
    return if (RADIO_GRUPPE_TIMER_PROSENT === sporsmal.svartype)
        sporsmal.undersporsmal!!
                .map { it.undersporsmal }
                .flatMap { it!! }
                .toList()
    else
        sporsmal.undersporsmal
}

private fun formatterSporsmalOgSvar(sporsmal: Sporsmal): List<String> {
    return when (sporsmal.svartype) {
        CHECKBOX, CHECKBOX_GRUPPE, RADIO, RADIO_GRUPPE, RADIO_GRUPPE_TIMER_PROSENT ->
                    listOfNotNull(sporsmal.sporsmalstekst)
        JA_NEI ->   listOfNotNull(sporsmal.sporsmalstekst, if ("JA" == getForsteSvarverdi(sporsmal)) "Ja" else "Nei")
        DATO ->     listOfNotNull(sporsmal.sporsmalstekst, formatterDato(getForsteSvarverdi(sporsmal)))
        PERIODE ->  listOfNotNull(sporsmal.sporsmalstekst, formatterPeriode(getForsteSvarverdi(sporsmal)))
        PERIODER -> listOfNotNull(sporsmal.sporsmalstekst) + getSvarverdier(sporsmal).map { formatterPeriode(it) }
        LAND ->     listOfNotNull(sporsmal.sporsmalstekst) + getSvarverdier(sporsmal).map { formatterLand(it) }
        TALL ->     listOfNotNull(sporsmal.sporsmalstekst, getForsteSvarverdi(sporsmal) + " " + sporsmal.undertekst)
        TIMER ->    listOfNotNull(sporsmal.sporsmalstekst, getForsteSvarverdi(sporsmal) + " timer")
        PROSENT ->  listOfNotNull(sporsmal.sporsmalstekst, getForsteSvarverdi(sporsmal) + " prosent")
        FRITEKST -> listOfNotNull(sporsmal.sporsmalstekst, getForsteSvarverdi(sporsmal))
        else ->     emptyList()
    }
}

private fun getSvarverdier(sporsmal: Sporsmal): List<String> {
    return sporsmal.svar?.mapNotNull { it.verdi } ?: emptyList()
}

private fun getForsteSvarverdi(sporsmal: Sporsmal): String {
    return sporsmal.svar?.firstOrNull()?.verdi ?: ""
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

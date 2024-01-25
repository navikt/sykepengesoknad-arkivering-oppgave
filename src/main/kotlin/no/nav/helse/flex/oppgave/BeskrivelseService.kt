package no.nav.helse.flex.oppgave

import meldingDersomAvbruttFristFeil
import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Arbeidssituasjon.ARBEIDSTAKER
import no.nav.helse.flex.domain.dto.Avsendertype.SYSTEM
import no.nav.helse.flex.domain.dto.Merknad
import no.nav.helse.flex.domain.dto.Soknadstype.*
import no.nav.helse.flex.domain.dto.Sporsmal
import no.nav.helse.flex.domain.dto.Svartype.*
import no.nav.helse.flex.tittel.skapTittel
import no.nav.helse.flex.util.DatoUtil.norskDato
import no.nav.helse.flex.util.PeriodeMapper.jsonTilPeriode
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*
import java.util.Collections.nCopies

val log = LoggerFactory.getLogger("no.nav.helse.oppgave.BeskrivelseService")

fun lagBeskrivelse(soknad: Soknad): String {
    return soknad.meldingDersomAvbruttFristFeil() +
        soknad.meldingDersomEgenmeldtSykmelding() +
        soknad.meldingDersomAvsendertypeErSystem() +
        soknad.skapTittel() +
        soknad.erKorrigert() + "\n" +
        soknad.beskrivMerknaderFraSykmelding() +
        soknad.beskrivKvitteringer() +
        soknad.beskrivArbeidsgiver() +
        soknad.beskrivPerioder() +
        soknad.beskrivFaktiskGradFrilansere() +
        soknad.beskrivMedlemskapVurdering() +
        soknad.sporsmal
            .filter { it.skalVises() }
            .map { sporsmal -> beskrivSporsmal(sporsmal, 0) }
            .filter { it.isNotBlank() }
            .joinToString("\n")
}

private fun Soknad.beskrivMerknaderFraSykmelding(): String {
    return if (merknaderFraSykmelding?.isNotEmpty() == true) {
        merknaderFraSykmelding
            .joinToString(separator = "\n", postfix = "\n") { it.beskrivMerknad() }
    } else {
        ""
    }
}

private fun Soknad.beskrivKvitteringer(): String {
    return if (kvitteringer != null && kvitteringer.isNotEmpty()) {
        "\nSøknaden har vedlagt ${kvitteringer.size} kvitteringer med en sum på ${
            kvitteringSum.toString().formatterBelop()
        } kr\n"
    } else {
        ""
    }
}

private fun Merknad.beskrivMerknad(): String {
    return when (type) {
        "UGYLDIG_TILBAKEDATERING" -> "OBS! Sykmeldingen er avslått grunnet ugyldig tilbakedatering"
        "TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER" -> "OBS! Tilbakedatert sykmelding er til vurdering"
        "TILBAKEDATERT_PAPIRSYKMELDING" -> "OBS! Sykmeldingen sendt til NAY for manuell kontroll grunnet tilbakedatering"
        "UNDER_BEHANDLING" ->
            "OBS! Sykmeldingen er tilbakedatert. Tilbakedateringen var ikke behandlet når søknaden " +
                "ble sendt. Sjekk gosys for resultat på tilbakedatering"
        else -> {
            log.warn("Ukjent merknadstype $type")
            "OBS! Sykmeldingen har en merknad $this"
        }
    }
}

private fun Soknad.meldingDersomEgenmeldtSykmelding() =
    if (egenmeldtSykmelding == true) {
        "Denne søknaden hører til en egenmeldt sykmelding\n"
    } else {
        ""
    }

private fun Soknad.meldingDersomAvsendertypeErSystem() =
    if (avsendertype == SYSTEM) {
        "Denne søknaden er autogenerert på grunn av et registrert dødsfall\n"
    } else {
        ""
    }

private fun Soknad.erKorrigert() = korrigerer?.let { " KORRIGERING" } ?: ""

private fun Soknad.beskrivArbeidsgiver() =
    if (arbeidssituasjon === ARBEIDSTAKER) {
        "\nArbeidsgiver: $arbeidsgiver" +
            "\nOrganisasjonsnummer: $orgNummer\n"
    } else {
        ""
    }

private fun Soknad.beskrivPerioder() =
    soknadPerioder?.mapIndexed { index, periode ->
        "\nPeriode ${index + 1}:\n" +
            "${periode.fom!!.format(norskDato)} - ${periode.tom!!.format(norskDato)}\n" +
            (
                periode.grad?.let { grad ->
                    if (soknadstype != REISETILSKUDD && soknadstype != BEHANDLINGSDAGER) {
                        "Grad: ${grad}\n"
                    } else {
                        ""
                    }
                } ?: ""
            ) +
            (
                periode.faktiskGrad?.let { faktiskGrad ->
                    "Oppgitt faktisk arbeidsgrad: $faktiskGrad\n"
                } ?: ""
            )
    }?.joinToString("") ?: ""

// Er egen beskrivelse for selvstendig/frilanser fordi SoknadDTO Periode ikke inneholder feltet faktiskGrad før Mars 2020, slik som arbeidstakere
private fun Soknad.beskrivFaktiskGradFrilansere(): String {
    if (soknadstype === SELVSTENDIGE_OG_FRILANSERE) {
        val harJobbetMerEnnGradert =
            sporsmal
                .asSequence()
                .filter { it.tag.startsWith("JOBBET_DU_GRADERT_") || it.tag.startsWith("JOBBET_DU_100_PROSENT_") }
                .any { it.svar?.asSequence()?.any { svar -> svar.verdi == "JA" } ?: false }

        val periodeMedFaktiskGrad = soknadPerioder?.any { it.faktiskGrad != null } ?: false

        if (harJobbetMerEnnGradert && !periodeMedFaktiskGrad) {
            return """
                
                OBS! Brukeren har jobbet mer enn uføregraden i sykmeldingen.
                Se oppgitt arbeidsgrad lengre ned i oppgaven
                
                """.trimIndent()
        }
    }
    return ""
}

private fun Soknad.beskrivMedlemskapVurdering(): String {
    if (medlemskapVurdering in listOf("UAVKLART", "NEI")) {
        return """
            
            Om bruker er medlem i folketrygden eller ikke, kunne ikke avklares automatisk.
            Medlemskap status: $medlemskapVurdering
            
            Du må se på svarene til bruker.
            Informasjon om hva du skal gjøre finner du på Navet, se
            https://navno.sharepoint.com/sites/fag-og-ytelser-eos-lovvalg-medlemskap/SitePages/Hvordan-vurderer-jeg-lovvalg-og-medlemskap.aspx
            
            """.trimIndent()
    }
    return ""
}

private fun Sporsmal.skalVises() =
    when (tag) {
        "ANSVARSERKLARING", "BEKREFT_OPPLYSNINGER", "EGENMELDINGER", "FRAVER_FOR_BEHANDLING" -> false
        "ARBEIDSGIVER" -> true
        "UTBETALING" -> true
        "FRISKMELDT" -> "NEI" == forsteSvarverdi()
        else -> "NEI" != forsteSvarverdi()
    }

private fun beskrivSporsmal(
    sporsmal: Sporsmal,
    dybde: Int,
): String {
    val innrykk = "\n" + nCopies(dybde, "    ").joinToString("")
    val svarverdier = sporsmal.svarverdier()

    if (svarverdier.isEmpty() && sporsmal.svartype !in
        listOf(
            CHECKBOX_GRUPPE,
            RADIO_GRUPPE,
            RADIO_GRUPPE_TIMER_PROSENT,
            INFO_BEHANDLINGSDAGER,
            GRUPPE_AV_UNDERSPORSMAL,
            IKKE_RELEVANT,
        )
    ) {
        return ""
    }

    val sporsmalBeskrivelse =
        sporsmal.formatterSporsmalOgSvar().joinToString("") { sporsmalOgSvar ->
            innrykk + sporsmalOgSvar
        }
    val undersporsmålBeskrivelse =
        sporsmal.undersporsmalIgnorerRadioIGruppeTimerProsent()
            ?.map { beskrivSporsmal(it, getNesteDybde(sporsmal, dybde)) }
            ?.filter { it.isNotBlank() }
            ?.joinToString("\n")
            ?.fjernTommeLinjerHvisMedlemskapsgruppe(sporsmal)
            ?: ""

    return sporsmalBeskrivelse.plus(undersporsmålBeskrivelse)
}

private fun String.fjernTommeLinjerHvisMedlemskapsgruppe(sporsmal: Sporsmal): String {
    if (sporsmal.svartype == GRUPPE_AV_UNDERSPORSMAL && sporsmal.tag.contains("MEDLEMSKAP")) {
        return this.replace("\n\n", "\n")
    }

    return this
}

private fun getNesteDybde(
    sporsmal: Sporsmal,
    dybde: Int,
): Int {
    return when (sporsmal.svartype) {
        RADIO_GRUPPE, RADIO_GRUPPE_TIMER_PROSENT, GRUPPE_AV_UNDERSPORSMAL -> dybde
        else -> dybde + 1
    }
}

private fun Sporsmal.undersporsmalIgnorerRadioIGruppeTimerProsent(): List<Sporsmal>? {
    return if (svartype === RADIO_GRUPPE_TIMER_PROSENT) {
        undersporsmal!!
            .filter { it.kriterieForVisningAvUndersporsmal?.name == it.forsteSvarverdi() }
            .map { it.undersporsmal }
            .flatMap { it!! }
            .toList()
    } else {
        undersporsmal
    }
}

private fun Sporsmal.formatterSporsmalOgSvar(): List<String> {
    return when (svartype) {
        CHECKBOX, CHECKBOX_GRUPPE, RADIO, RADIO_GRUPPE, RADIO_GRUPPE_TIMER_PROSENT, INFO_BEHANDLINGSDAGER ->
            listOfNotNull(sporsmalstekst)

        IKKE_RELEVANT -> emptyList()
        JA_NEI -> listOfNotNull(sporsmalstekst, if ("JA" == forsteSvarverdi()) "Ja" else "Nei")
        DATO -> listOfNotNull(sporsmalstekst, formatterDato(forsteSvarverdi()))
        PERIODE -> listOfNotNull(sporsmalstekst, formatterPeriode(forsteSvarverdi()))
        PERIODER -> listOfNotNull(sporsmalstekst) + svarverdier().map { formatterPeriode(it) }
        LAND, COMBOBOX_MULTI -> listOfNotNull(sporsmalstekst) + svarverdier().map { formatterLand(it) }
        TALL -> listOfNotNull(sporsmalstekst, forsteSvarverdi() + " " + undertekst.orEmpty())
        KILOMETER -> listOfNotNull(sporsmalstekst, forsteSvarverdi() + " km")
        BELOP -> listOfNotNull(sporsmalstekst, forsteSvarverdi().formatterBelop() + " kr")
        TIMER -> listOfNotNull(sporsmalstekst, forsteSvarverdi() + " timer")
        PROSENT -> listOfNotNull(sporsmalstekst, forsteSvarverdi() + " prosent")
        FRITEKST, COMBOBOX_SINGLE -> listOfNotNull(sporsmalstekst, forsteSvarverdi())
        RADIO_GRUPPE_UKEKALENDER -> listOfNotNull(formatterBehandlingsdato(forsteSvarverdi()))
        DATOER -> listOfNotNull(sporsmalstekst) + svarverdier().map { formatterDato(it) }
        else -> emptyList()
    }
}

private fun Sporsmal.svarverdier(): List<String> {
    return svar?.mapNotNull { it.verdi } ?: emptyList()
}

private fun Sporsmal.forsteSvarverdi(): String {
    return svar?.firstOrNull()?.verdi ?: ""
}

private fun String.formatterBelop(): String {
    val context = this.toInt()
    val kr = context / 100
    val øre = context % 100
    @Suppress("DEPRECATION")
    return "%,d,%02d".format(locale = Locale("nb"), kr, øre)
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

private fun formatterBehandlingsdato(svarverdi: String): String {
    return try {
        formatterDato(svarverdi)
    } catch (e: Exception) {
        "Ikke til behandling"
    }
}

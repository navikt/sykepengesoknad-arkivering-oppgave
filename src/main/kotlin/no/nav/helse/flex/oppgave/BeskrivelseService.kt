package no.nav.helse.flex.oppgave

import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Arbeidssituasjon.ARBEIDSTAKER
import no.nav.helse.flex.domain.dto.Avsendertype.SYSTEM
import no.nav.helse.flex.domain.dto.Merknad
import no.nav.helse.flex.domain.dto.SigrunInntekt
import no.nav.helse.flex.domain.dto.Soknadstype.*
import no.nav.helse.flex.domain.dto.Sporsmal
import no.nav.helse.flex.domain.dto.Svartype.*
import no.nav.helse.flex.domain.dto.harInntektsopplysninger
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.tittel.skapTittel
import no.nav.helse.flex.util.DatoUtil.norskDato
import no.nav.helse.flex.util.PeriodeMapper.jsonTilPeriode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*
import java.util.Collections.nCopies

val log: Logger = LoggerFactory.getLogger("no.nav.helse.oppgave.BeskrivelseService")

fun lagBeskrivelse(soknad: Soknad): String =
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
        soknad.beskrivInntektsopplysninger() +
        soknad.sporsmal
            .filter { it.skalVises(soknad.medlemskapVurdering) }
            .map { sporsmal -> beskrivSporsmal(sporsmal, 0) }
            .filter { it.isNotBlank() }
            .joinToString("\n")

private fun Soknad.beskrivMerknaderFraSykmelding(): String =
    if (merknaderFraSykmelding?.isNotEmpty() == true) {
        merknaderFraSykmelding
            .joinToString(separator = "\n", postfix = "\n") { it.beskrivMerknad() }
    } else {
        ""
    }

private fun Soknad.beskrivKvitteringer(): String =
    if (kvitteringer != null && kvitteringer.isNotEmpty()) {
        "\nSøknaden har vedlagt ${kvitteringer.size} kvitteringer med en sum på ${
            kvitteringSum.toString().formatterBelop()
        } kr\n"
    } else {
        ""
    }

private fun Merknad.beskrivMerknad(): String =
    when (type) {
        "UGYLDIG_TILBAKEDATERING" -> "OBS! Sykmeldingen er avslått grunnet ugyldig tilbakedatering"
        "TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER" -> "OBS! Tilbakedatert sykmelding er til vurdering"
        "TILBAKEDATERT_PAPIRSYKMELDING" -> "OBS! Sykmeldingen sendt til NAY for manuell kontroll grunnet tilbakedatering"
        "DELVIS_GODKJENT" -> "OBS! Tilbakedatert sykmelding er delvis godkjent"
        "UNDER_BEHANDLING" ->
            "OBS! Sykmeldingen er tilbakedatert. Tilbakedateringen var ikke behandlet når søknaden " +
                "ble sendt. Sjekk gosys for resultat på tilbakedatering"

        else -> {
            log.warn("Ukjent merknadstype $type")
            "OBS! Sykmeldingen har en merknad $this"
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
    soknadPerioder
        ?.mapIndexed { index, periode ->
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

private fun Soknad.beskrivMedlemskapVurdering(): String =
    when (medlemskapVurdering) {
        "UAVKLART" ->
            """
            
            Om bruker er medlem i folketrygden eller ikke, kunne ikke avklares automatisk.
            Medlemskap status: UAVKLART
            
            Du må se på svarene til bruker.
            Informasjon om hva du skal gjøre finner du på Navet, se
            https://navno.sharepoint.com/sites/fag-og-ytelser-eos-lovvalg-medlemskap/SitePages/Hvordan-vurderer-jeg-lovvalg-og-medlemskap.aspx
            
            """.trimIndent()

        "NEI" ->
            """

            Om bruker er medlem i folketrygden eller ikke er automatisk avklart.
            Medlemskap status: NEI

            Se på medlemskapsfanen i Gosys for å finne riktig periode.
            Se på dokumentet i Gosys for å finne arbeidsgiver.
            Se i Aa-registeret om bruker har samme arbeidsgiver som i vedtaket/A1 fra utlandet.
            Hvis Ja: Bruker er ikke medlem. Hvis Nei: Kontakt bruker/arbeidsgiver for å avklare brukers situasjon.

            """.trimIndent()

        else -> ""
    }

private fun Soknad.beskrivInntektsopplysninger(): String {
    if (sporsmal.harInntektsopplysninger()) {
        return """
            
            Inntektsopplysninger
            
            """.trimIndent()
    }
    return ""
}

private fun Sporsmal.skalVises(medlemskapVurdering: String?): Boolean {
    // Hvis endelig medlemskapsvurdering er JA (avklart) trenger vi ikke å vise medlemskapsspørsmålene med tilhørende
    // svar som eventuelt gjorde at vurderingen gikk fra uavklart til avklart siden det bare blir for saksbehandler.
    if (tag.startsWith("MEDLEMSKAP_") && medlemskapVurdering == "JA") {
        return false
    }

    // FTA spørsmål om inntekt er alltid relevant å se uavhengig av svar
    if (tag == "FTA_INNTEKT_UNDERVEIS") {
        return true
    }

    return when (tag) {
        "ANSVARSERKLARING", "BEKREFT_OPPLYSNINGER", "EGENMELDINGER", "FRAVER_FOR_BEHANDLING" -> false
        "ARBEIDSGIVER" -> true
        "UTBETALING" -> true
        "FRISKMELDT" -> "NEI" == forsteSvarverdi()
        else -> "NEI" != forsteSvarverdi()
    }
}

private fun beskrivSporsmal(
    sporsmal: Sporsmal,
    dybde: Int,
): String {
    val innrykk = "\n" + nCopies(dybde, "    ").joinToString("")
    val svarverdier = sporsmal.svarverdier()

    if (svarverdier.isEmpty() &&
        sporsmal.svartype !in
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
        sporsmal
            .undersporsmalIgnorerRadioIGruppeTimerProsent()
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
): Int =
    when (sporsmal.svartype) {
        RADIO_GRUPPE, RADIO_GRUPPE_TIMER_PROSENT, GRUPPE_AV_UNDERSPORSMAL -> dybde
        else -> dybde + 1
    }

private fun Sporsmal.undersporsmalIgnorerRadioIGruppeTimerProsent(): List<Sporsmal>? =
    if (svartype === RADIO_GRUPPE_TIMER_PROSENT) {
        undersporsmal!!
            .filter { it.kriterieForVisningAvUndersporsmal?.name == it.forsteSvarverdi() }
            .map { it.undersporsmal }
            .flatMap { it!! }
            .toList()
    } else {
        undersporsmal
    }

private fun Sporsmal.formatterSporsmalOgSvar(): List<String> =
    when (svartype) {
        CHECKBOX, CHECKBOX_GRUPPE, RADIO, RADIO_GRUPPE, RADIO_GRUPPE_TIMER_PROSENT, INFO_BEHANDLINGSDAGER ->
            listOfNotNull(sporsmalstekst)

        IKKE_RELEVANT -> emptyList()
        JA_NEI -> {
            val sporsmalOgSvar = listOfNotNull(sporsmalstekst, if ("JA" == forsteSvarverdi()) "Ja" else "Nei")
            when (tag) {
                "INNTEKTSOPPLYSNINGER_VARIG_ENDRING_25_PROSENT" -> {
                    sigrunData(sporsmalOgSvar)
                }

                else -> sporsmalOgSvar
            }
        }

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

private fun Sporsmal.svarverdier(): List<String> = svar?.mapNotNull { it.verdi } ?: emptyList()

private fun Sporsmal.forsteSvarverdi(): String = svar?.firstOrNull()?.verdi ?: ""

val norskLocale =
    Locale
        .Builder()
        .setLanguage("nb")
        .setRegion("NO")
        .build()

private fun String.formatterBelop(): String {
    val context = this.toInt()
    val kr = context / 100
    val øre = context % 100
    return "%,d,%02d".format(locale = norskLocale, kr, øre)
}

private fun formatterDato(svarverdi: String?): String = LocalDate.parse(svarverdi!!).format(norskDato)

private fun formatterPeriode(svarverdi: String?): String {
    val (fom, tom) = jsonTilPeriode(svarverdi)
    return fom.format(norskDato) + " - " +
        tom.format(norskDato)
}

private fun formatterLand(svarverdi: String): String = "- $svarverdi"

private fun formatterBehandlingsdato(svarverdi: String): String =
    try {
        formatterDato(svarverdi)
    } catch (e: Exception) {
        "Ikke til behandling"
    }

private fun Sporsmal.sigrunData(sporsmalOgSvar: List<String>): List<String> {
    val grunnlag =
        try {
            objectMapper.convertValue(metadata?.get("sigrunInntekt"), SigrunInntekt::class.java)
                ?: return sporsmalOgSvar
        } catch (e: Exception) {
            log.warn(e.message)
            return sporsmalOgSvar
        }

    val snittTekst = "Gjennomsnittlig årsinntekt på sykmeldingstidspunktet: ${
        grunnlag.beregnet.snitt.toString().formaterInntekt()
    } kroner"
    val lignedeAarTekst = "Inntekt per kalenderår, de tre siste ferdiglignede årene: "
    val lignedeAarVerdier = grunnlag.inntekter.map { "${it.aar}: " + it.verdi.toString().formaterInntekt() + " kroner" }

    return sporsmalOgSvar
        .toMutableList()
        .apply {
            add(snittTekst)
            add(lignedeAarTekst)
        }.plus(lignedeAarVerdier)
}

/**
 * @return 1500000 -> 1 500 000
 */
private fun String.formaterInntekt(): String =
    this
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()

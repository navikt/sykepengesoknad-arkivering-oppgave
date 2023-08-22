package no.nav.helse.flex.kafka.mapper

import no.nav.helse.flex.domain.dto.*
import no.nav.helse.flex.sykepengesoknad.kafka.*

private fun SvarDTO.toSvar(): Svar =
    Svar(verdi)

private fun SporsmalDTO.toSporsmal(): Sporsmal =
    Sporsmal(
        id = id!!,
        tag = tag!!,
        sporsmalstekst = sporsmalstekst,
        undertekst = undertekst,
        svartype = svartype?.mapSvartype(),
        min = min,
        max = max,
        kriterieForVisningAvUndersporsmal = kriterieForVisningAvUndersporsmal?.mapKriterie(),
        svar = svar!!.map { it.toSvar() },
        undersporsmal = undersporsmal?.map { it.toSporsmal() }
    )

private fun VisningskriteriumDTO.mapKriterie(): Visningskriterie = when (this) {
    VisningskriteriumDTO.CHECKED -> Visningskriterie.CHECKED
    VisningskriteriumDTO.NEI -> Visningskriterie.NEI
    VisningskriteriumDTO.JA -> Visningskriterie.JA
}

private fun SvartypeDTO.mapSvartype(): Svartype {
    return when (this) {
        SvartypeDTO.JA_NEI -> Svartype.JA_NEI
        SvartypeDTO.CHECKBOX -> Svartype.CHECKBOX
        SvartypeDTO.CHECKBOX_GRUPPE -> Svartype.CHECKBOX_GRUPPE
        SvartypeDTO.CHECKBOX_PANEL -> Svartype.CHECKBOX_PANEL
        SvartypeDTO.DATO -> Svartype.DATO
        SvartypeDTO.PERIODE -> Svartype.PERIODE
        SvartypeDTO.PERIODER -> Svartype.PERIODER
        SvartypeDTO.TIMER -> Svartype.TIMER
        SvartypeDTO.FRITEKST -> Svartype.FRITEKST
        SvartypeDTO.IKKE_RELEVANT -> Svartype.IKKE_RELEVANT
        SvartypeDTO.PROSENT -> Svartype.PROSENT
        SvartypeDTO.RADIO_GRUPPE -> Svartype.RADIO_GRUPPE
        SvartypeDTO.RADIO_GRUPPE_TIMER_PROSENT -> Svartype.RADIO_GRUPPE_TIMER_PROSENT
        SvartypeDTO.RADIO -> Svartype.RADIO
        SvartypeDTO.TALL -> Svartype.TALL
        SvartypeDTO.RADIO_GRUPPE_UKEKALENDER -> Svartype.RADIO_GRUPPE_UKEKALENDER
        SvartypeDTO.LAND -> Svartype.LAND
        SvartypeDTO.INFO_BEHANDLINGSDAGER -> Svartype.INFO_BEHANDLINGSDAGER
        SvartypeDTO.KVITTERING -> Svartype.KVITTERING
        SvartypeDTO.DATOER -> Svartype.DATOER
        SvartypeDTO.BELOP -> Svartype.BELOP
        SvartypeDTO.KILOMETER -> Svartype.KILOMETER
        SvartypeDTO.COMBOBOX_SINGLE -> Svartype.COMBOBOX_SINGLE
        SvartypeDTO.COMBOBOX_MULTI -> Svartype.COMBOBOX_MULTI
    }
}

private fun SoknadsperiodeDTO.toSoknadPeriode(): SoknadPeriode =
    SoknadPeriode(
        fom = fom,
        tom = tom,
        grad = sykmeldingsgrad,
        faktiskGrad = faktiskGrad
    )

private fun SoknadstypeDTO.tilSoknadstype(): Soknadstype =
    when (this) {
        SoknadstypeDTO.SELVSTENDIGE_OG_FRILANSERE -> Soknadstype.SELVSTENDIGE_OG_FRILANSERE
        SoknadstypeDTO.OPPHOLD_UTLAND -> Soknadstype.OPPHOLD_UTLAND
        SoknadstypeDTO.ARBEIDSTAKERE -> Soknadstype.ARBEIDSTAKERE
        SoknadstypeDTO.ANNET_ARBEIDSFORHOLD -> Soknadstype.ANNET_ARBEIDSFORHOLD
        SoknadstypeDTO.ARBEIDSLEDIG -> Soknadstype.ARBEIDSLEDIG
        SoknadstypeDTO.BEHANDLINGSDAGER -> Soknadstype.BEHANDLINGSDAGER
        SoknadstypeDTO.REISETILSKUDD -> Soknadstype.REISETILSKUDD
        SoknadstypeDTO.GRADERT_REISETILSKUDD -> Soknadstype.GRADERT_REISETILSKUDD
    }

fun SykepengesoknadDTO.toSykepengesoknad(
    aktorId: String
): Sykepengesoknad {
    return Sykepengesoknad(
        id = id,
        sykmeldingId = sykmeldingId,
        aktorId = aktorId,
        soknadstype = type.tilSoknadstype(),
        status = status.name,
        fom = fom,
        tom = tom,
        opprettet = opprettet!!,
        sendtNav = sendtNav,
        sendtArbeidsgiver = sendtArbeidsgiver,
        arbeidsgiver = arbeidsgiver?.navn,
        arbeidssituasjon = arbeidssituasjon?.mapArbeidssituasjon(),
        startSykeforlop = startSyketilfelle,
        sykmeldingSkrevet = sykmeldingSkrevet,
        korrigertAv = korrigertAv,
        korrigerer = korrigerer,
        soknadPerioder = soknadsperioder?.map { it.toSoknadPeriode() },
        sporsmal = sporsmal!!.map { it.toSporsmal() },
        avsendertype = avsendertype?.mapAvsendertype(),
        ettersending = ettersending,
        egenmeldtSykmelding = egenmeldtSykmelding,
        orgNummer = arbeidsgiver?.orgnummer,
        harRedusertVenteperiode = harRedusertVenteperiode ?: false,
        merknaderFraSykmelding = merknaderFraSykmelding?.map { Merknad(type = it.type, beskrivelse = it.beskrivelse) },
        merknader = merknader,
        sendTilGosys = sendTilGosys,
        utenlandskSykmelding = utenlandskSykmelding,
        egenmeldingsdagerFraSykmelding = egenmeldingsdagerFraSykmelding
    )
}

private fun AvsendertypeDTO.mapAvsendertype(): Avsendertype = when (this) {
    AvsendertypeDTO.BRUKER -> Avsendertype.BRUKER
    AvsendertypeDTO.SYSTEM -> Avsendertype.SYSTEM
}

private fun ArbeidssituasjonDTO.mapArbeidssituasjon(): Arbeidssituasjon = when (this) {
    ArbeidssituasjonDTO.SELVSTENDIG_NARINGSDRIVENDE -> Arbeidssituasjon.NAERINGSDRIVENDE
    ArbeidssituasjonDTO.FRILANSER -> Arbeidssituasjon.FRILANSER
    ArbeidssituasjonDTO.ARBEIDSTAKER -> Arbeidssituasjon.ARBEIDSTAKER
    ArbeidssituasjonDTO.ARBEIDSLEDIG -> Arbeidssituasjon.ARBEIDSLEDIG
    ArbeidssituasjonDTO.ANNET -> Arbeidssituasjon.ANNET
}

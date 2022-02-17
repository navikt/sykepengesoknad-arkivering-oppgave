package no.nav.helse.kafka.mapper

import no.nav.helse.domain.dto.*
import no.nav.helse.flex.sykepengesoknad.kafka.*

private inline fun <T : Enum<*>, reified U : Enum<*>> T?.enumValueOrNull(): U? =
    U::class.java.enumConstants.firstOrNull { it.name == this?.name }

private fun SvarDTO.toSvar(): Svar =
    Svar(verdi)

private fun SporsmalDTO.toSporsmal(): Sporsmal =
    Sporsmal(
        id = id!!,
        tag = tag!!,
        sporsmalstekst = sporsmalstekst,
        undertekst = undertekst,
        svartype = svartype.enumValueOrNull(),
        min = min,
        max = max,
        kriterieForVisningAvUndersporsmal = kriterieForVisningAvUndersporsmal.enumValueOrNull(),
        svar = svar!!.map { it.toSvar() },
        undersporsmal = undersporsmal?.map { it.toSporsmal() }
    )

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
        arbeidssituasjon = arbeidssituasjon.enumValueOrNull(),
        startSykeforlop = startSyketilfelle,
        sykmeldingSkrevet = sykmeldingSkrevet,
        korrigertAv = korrigertAv,
        korrigerer = korrigerer,
        soknadPerioder = soknadsperioder?.map { it.toSoknadPeriode() },
        sporsmal = sporsmal!!.map { it.toSporsmal() },
        avsendertype = avsendertype.enumValueOrNull(),
        ettersending = ettersending,
        egenmeldtSykmelding = egenmeldtSykmelding,
        orgNummer = arbeidsgiver?.orgnummer,
        harRedusertVenteperiode = harRedusertVenteperiode ?: false,
        merknaderFraSykmelding = merknaderFraSykmelding?.map { Merknad(type = it.type, beskrivelse = it.beskrivelse) }

    )
}

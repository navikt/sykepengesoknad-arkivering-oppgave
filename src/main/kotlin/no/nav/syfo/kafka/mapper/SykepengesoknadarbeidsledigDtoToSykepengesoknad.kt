package no.nav.syfo.kafka.mapper

import no.nav.syfo.domain.dto.*
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SoknadsperiodeDTO
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SporsmalDTO
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SvarDTO
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SykepengesoknadArbeidsledigDTO

private inline fun <T : Enum<*>, reified U : Enum<*>> T?.enumValueOrNull(): U? =
        U::class.java.enumConstants.firstOrNull { it.name == this?.name }


private fun SporsmalDTO.toSporsmal(): Sporsmal =
    Sporsmal(
        id = id,
        tag = tag,
        sporsmalstekst = sporsmalstekst,
        undertekst = undertekst,
        svartype = svartype?.enumValueOrNull(),
        min = min,
        max = max,
        kriterieForVisningAvUndersporsmal = kriteriumForVisningAvUndersporsmal?.enumValueOrNull(),
        svar = svar.map { it.toSvar() },
        undersporsmal = undersporsmal.map { it.toSporsmal() }
    )

private fun SvarDTO.toSvar(): Svar =
    Svar(verdi)

private fun SoknadsperiodeDTO.toSoknadPeriode(): SoknadPeriode =
    SoknadPeriode(
        fom = fom,
        tom = tom,
        grad = sykmeldingsgrad)

fun SykepengesoknadArbeidsledigDTO.toSykepengesoknad(): Sykepengesoknad =
    Sykepengesoknad(
        id = id!!,
        sykmeldingId = sykmeldingId,
        aktorId = aktorId!!,
        soknadstype = Soknadstype.ARBEIDSLEDIG,
        status = status!!.name,
        fom = fom,
        tom = tom,
        opprettet = opprettet!!,
        sendtNav = sendtNav,
        arbeidssituasjon = Arbeidssituasjon.ARBEIDSLEDIG,
        startSykeforlop = startSyketilfelle,
        sykmeldingSkrevet = sykmeldingSkrevet,
        korrigertAv = korrigertAv,
        korrigerer = korrigerer,
        soknadPerioder = soknadsperioder!!.map { it.toSoknadPeriode() },
        sporsmal = sporsmal!!.map { it.toSporsmal() },
        avsendertype = avsendertype.enumValueOrNull() )

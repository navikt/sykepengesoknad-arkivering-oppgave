package no.nav.syfo.kafka.mapper

import no.nav.syfo.domain.dto.SoknadPeriode
import no.nav.syfo.domain.dto.Sporsmal
import no.nav.syfo.domain.dto.Svar
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SoknadsperiodeDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SporsmalDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SvarDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO


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
                kriterieForVisningAvUndersporsmal = kriteriumForVisningAvUndersporsmal.enumValueOrNull(),
                svar = svar!!.map { it.toSvar() },
                undersporsmal = undersporsmal!!.map { it.toSporsmal() }
        )


private fun SoknadsperiodeDTO.toSoknadPeriode(): SoknadPeriode =
        SoknadPeriode(
                fom = fom,
                tom = tom,
                grad = sykmeldingsgrad,
                faktiskGrad = faktiskGrad)


fun SykepengesoknadDTO.toSykepengesoknad(): Sykepengesoknad {
    return Sykepengesoknad(
            id = id!!,
            sykmeldingId = sykmeldingId,
            aktorId = aktorId!!,
            soknadstype = type.enumValueOrNull(),
            status = status!!.name,
            fom = fom,
            tom = tom,
            opprettet = opprettet!!,
            sendtNav = sendtNav,
            sendtArbeidsgiver = sendtArbeidsgiver,
            arbeidsgiver = arbeidsgiver!!.navn,
            arbeidssituasjon = arbeidssituasjon.enumValueOrNull(),
            startSykeforlop = startSyketilfelle,
            sykmeldingSkrevet = sykmeldingSkrevet,
            korrigertAv = korrigertAv,
            korrigerer = korrigerer,
            soknadPerioder = soknadsperioder!!.map { it.toSoknadPeriode() },
            sporsmal = sporsmal!!.map { it.toSporsmal()},
            avsendertype = avsendertype.enumValueOrNull(),
            ettersending = ettersending
    )
}


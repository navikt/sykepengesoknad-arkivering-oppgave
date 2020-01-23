package no.nav.syfo.kafka.mapper

import no.nav.syfo.domain.dto.*
import no.nav.syfo.kafka.felles.SoknadsperiodeDTO
import no.nav.syfo.kafka.felles.SporsmalDTO
import no.nav.syfo.kafka.felles.SvarDTO
import no.nav.syfo.kafka.sykepengesoknadbehandlingsdager.dto.SykepengesoknadBehandlingsdagerDTO


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
                undersporsmal = undersporsmal!!.map { it.toSporsmal() }
        )


private fun SoknadsperiodeDTO.toSoknadPeriode(): SoknadPeriode =
        SoknadPeriode(
                fom = fom,
                tom = tom,
                sykmeldingstype = sykmeldingstype.toString())


fun SykepengesoknadBehandlingsdagerDTO.toSykepengesoknad(): Sykepengesoknad {
    return Sykepengesoknad(
            id = soknadFelles.id,
            sykmeldingId = sykepengesoknadFelles.sykmeldingId,
            aktorId = soknadFelles.aktorId,
            soknadstype = Soknadstype.BEHANDLINGSDAGER,
            status = soknadFelles.status.name,
            fom = sykepengesoknadFelles.fom,
            tom = sykepengesoknadFelles.tom,
            opprettet = soknadFelles.opprettet,
            sendtNav = soknadFelles.sendtNav,
            sendtArbeidsgiver = soknadFelles.sendtArbeidsgiver,
            arbeidsgiver = soknadFelles.arbeidsgiver?.navn,
            arbeidssituasjon = soknadFelles.arbeidssituasjon.enumValueOrNull(),
            startSykeforlop = sykepengesoknadFelles.startSyketilfelle,
            sykmeldingSkrevet = sykepengesoknadFelles.sykmeldingSkrevet,
            korrigertAv = soknadFelles.korrigertAv,
            korrigerer = soknadFelles.korrigerer,
            soknadPerioder = sykepengesoknadFelles.soknadsperioder.map { it.toSoknadPeriode() },
            sporsmal = soknadFelles.sporsmal.map { it.toSporsmal() },
            avsendertype = soknadFelles.avsendertype.enumValueOrNull(),
            ettersending = soknadFelles.ettersending
    )
}


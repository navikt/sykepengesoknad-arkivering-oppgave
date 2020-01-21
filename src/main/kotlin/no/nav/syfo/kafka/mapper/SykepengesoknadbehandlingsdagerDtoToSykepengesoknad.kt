package no.nav.syfo.kafka.mapper

import no.nav.syfo.domain.dto.*
import no.nav.syfo.kafka.sykepengesoknadbehandlingsdager.dto.SoknadsperiodeDTO
import no.nav.syfo.kafka.sykepengesoknadbehandlingsdager.dto.SporsmalDTO
import no.nav.syfo.kafka.sykepengesoknadbehandlingsdager.dto.SvarDTO
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
                kriterieForVisningAvUndersporsmal = kriteriumForVisningAvUndersporsmal.enumValueOrNull(),
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
            id = id,
            sykmeldingId = sykmeldingId,
            aktorId = aktorId,
            soknadstype = Soknadstype.BEHANDLINGSDAGER,
            status = status.name,
            fom = fom,
            tom = tom,
            opprettet = opprettet,
            sendtNav = sendtNav,
            sendtArbeidsgiver = sendtArbeidsgiver,
            arbeidsgiver = arbeidsgiver?.navn,
            arbeidssituasjon = arbeidssituasjon.enumValueOrNull(),
            startSykeforlop = startSyketilfelle,
            sykmeldingSkrevet = sykmeldingSkrevet,
            korrigertAv = korrigertAv,
            korrigerer = korrigerer,
            soknadPerioder = soknadsperioder.map { it.toSoknadPeriode() },
            sporsmal = sporsmal.map { it.toSporsmal() },
            avsendertype = avsendertype.enumValueOrNull(),
            ettersending = ettersending
    )
}


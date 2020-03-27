package no.nav.syfo.kafka.mapper

import no.nav.syfo.domain.dto.SoknadPeriode
import no.nav.syfo.domain.dto.Sporsmal
import no.nav.syfo.domain.dto.Svar
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.felles.SoknadsperiodeDTO
import no.nav.syfo.kafka.felles.SporsmalDTO
import no.nav.syfo.kafka.felles.SvarDTO
import no.nav.syfo.kafka.soknad.dto.SoknadDTO


private inline fun <reified U : Enum<*>> String?.stringEnumValueOrNull(): U? =
        U::class.java.enumConstants.firstOrNull { it.name == this }


private inline fun <T : Enum<*>, reified U : Enum<*>> T?.enumValueOrNull(): U? =
        U::class.java.enumConstants.firstOrNull { it.name == this?.name }

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
                svar = svar?.map { it.toSvar() },
                undersporsmal = undersporsmal?.map { it.toSporsmal() }
        )

private fun SvarDTO.toSvar(): Svar =
        Svar(verdi)

private fun SoknadsperiodeDTO.toSoknadPeriode(): SoknadPeriode =
        SoknadPeriode(
                fom = fom,
                tom = tom,
                grad = grad,
                faktiskGrad = faktiskGrad)

fun SoknadDTO.toSykepengesoknad(): Sykepengesoknad =
        Sykepengesoknad(
                id = id!!,
                sykmeldingId = sykmeldingId,
                aktorId = aktorId!!,
                soknadstype = soknadstype.stringEnumValueOrNull(),
                status = status!!,
                fom = fom,
                tom = tom,
                opprettet = opprettetDato!!.atStartOfDay(),
                sendtNav = innsendtDato?.atStartOfDay(),
                arbeidsgiver = arbeidsgiver,
                arbeidssituasjon = arbeidssituasjon.stringEnumValueOrNull(),
                startSykeforlop = startSykeforlop,
                sykmeldingSkrevet = sykmeldingUtskrevet?.atStartOfDay(),
                korrigertAv = korrigertAv,
                korrigerer = korrigerer,
                soknadPerioder = soknadPerioder?.map { it.toSoknadPeriode() },
                sporsmal = sporsmal!!.map { it.toSporsmal() },
                avsendertype = avsendertype?.name?.stringEnumValueOrNull(),
                egenmeldtSykmelding = egenmeldtSykmelding,
                harRedusertVenteperiode = harRedusertVenteperiode ?: false
        )



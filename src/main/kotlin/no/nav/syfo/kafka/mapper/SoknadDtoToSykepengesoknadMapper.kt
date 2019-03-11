package no.nav.syfo.kafka.mapper

import no.nav.syfo.domain.dto.SoknadPeriode
import no.nav.syfo.domain.dto.Sporsmal
import no.nav.syfo.domain.dto.Svar
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.soknad.dto.SoknadPeriodeDTO
import no.nav.syfo.kafka.soknad.dto.SporsmalDTO
import no.nav.syfo.kafka.soknad.dto.SvarDTO

object SoknadDtoToSykepengesoknadMapper {

    private inline fun <reified U : Enum<*>> String?.enumValueOrNull(): U? =
            U::class.java.enumConstants.firstOrNull { it.name == this }

    private fun konverter(svar: SvarDTO): Svar {
        return Svar(svar.verdi)
    }

    private fun konverter(sporsmal: SporsmalDTO): Sporsmal {
        return Sporsmal(
                id = sporsmal.id,
                tag = sporsmal.tag,
                sporsmalstekst = sporsmal.sporsmalstekst,
                undertekst = sporsmal.undertekst,
                svartype = sporsmal.svartype.enumValueOrNull(),
                min = sporsmal.min,
                max = sporsmal.max,
                kriterieForVisningAvUndersporsmal = sporsmal.kriterieForVisningAvUndersporsmal.enumValueOrNull(),
                svar = sporsmal.svar.map { SoknadDtoToSykepengesoknadMapper.konverter(it) },
                undersporsmal = sporsmal.undersporsmal.map { SoknadDtoToSykepengesoknadMapper.konverter(it) }
        )
    }

    private fun konverter(soknadPeriode: SoknadPeriodeDTO): SoknadPeriode {
        return SoknadPeriode(
                fom = soknadPeriode.fom,
                tom = soknadPeriode.tom,
                grad = soknadPeriode.grad)
    }

    fun konverter(soknad: SoknadDTO): Sykepengesoknad {
        return Sykepengesoknad(
                id = soknad.id,
                sykmeldingId = soknad.sykmeldingId,
                aktorId = soknad.aktorId,
                soknadstype = soknad.soknadstype.enumValueOrNull(),
                status = soknad.status,
                fom = soknad.fom,
                tom = soknad.tom,
                opprettet = soknad.opprettetDato.atStartOfDay(),
                sendtNav = soknad.innsendtDato?.atStartOfDay(),
                arbeidsgiver = soknad.arbeidsgiver,
                arbeidssituasjon = soknad.arbeidssituasjon.enumValueOrNull(),
                startSykeforlop = soknad.startSykeforlop,
                sykmeldingSkrevet = soknad.sykmeldingUtskrevet?.atStartOfDay(),
                korrigertAv = soknad.korrigertAv,
                korrigerer = soknad.korrigerer,
                soknadPerioder = soknad.soknadPerioder.map { SoknadDtoToSykepengesoknadMapper.konverter(it) },
                sporsmal = soknad.sporsmal.map { SoknadDtoToSykepengesoknadMapper.konverter(it) })
    }
}

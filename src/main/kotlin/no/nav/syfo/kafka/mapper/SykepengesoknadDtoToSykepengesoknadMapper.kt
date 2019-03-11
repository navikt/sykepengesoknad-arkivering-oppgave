package no.nav.syfo.kafka.mapper

import no.nav.syfo.domain.dto.SoknadPeriode
import no.nav.syfo.domain.dto.Sporsmal
import no.nav.syfo.domain.dto.Svar
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SoknadsperiodeDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SporsmalDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SvarDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import java.util.stream.Stream

object SykepengesoknadDtoToSykepengesoknadMapper {

    private inline fun <T:Enum<*>, reified U : Enum<*>> T?.enumValueOrNull(): U? =
            U::class.java.enumConstants.firstOrNull { it.name == this?.name }

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
                kriterieForVisningAvUndersporsmal = sporsmal.kriteriumForVisningAvUndersporsmal.enumValueOrNull(),
                svar = sporsmal.svar.map { SykepengesoknadDtoToSykepengesoknadMapper.konverter(it) },
                undersporsmal = sporsmal.undersporsmal.map { SykepengesoknadDtoToSykepengesoknadMapper.konverter(it) }
        )
    }

    private fun konverter(soknadPeriode: SoknadsperiodeDTO): SoknadPeriode {
        return SoknadPeriode(
                fom = soknadPeriode.fom,
                tom = soknadPeriode.tom,
                grad = soknadPeriode.sykmeldingsgrad,
                faktiskGrad = soknadPeriode.faktiskGrad)
    }

    fun konverter(sykepengesoknad: SykepengesoknadDTO): Sykepengesoknad {
        return Sykepengesoknad(
                id = sykepengesoknad.id,
                sykmeldingId =  sykepengesoknad.sykmeldingId,
                aktorId = sykepengesoknad.aktorId,
                soknadstype = sykepengesoknad.type.enumValueOrNull(),
                status = sykepengesoknad.status.name,
                fom = sykepengesoknad.fom,
                tom = sykepengesoknad.tom,
                opprettet = sykepengesoknad.opprettet,
                sendtNav = sykepengesoknad.sendtNav,
                sendtArbeidsgiver = sykepengesoknad.sendtArbeidsgiver,
                arbeidsgiver = sykepengesoknad.arbeidsgiver.navn,
                arbeidssituasjon = sykepengesoknad.arbeidssituasjon.enumValueOrNull(),
                startSykeforlop = sykepengesoknad.startSyketilfelle,
                sykmeldingSkrevet = sykepengesoknad.sykmeldingSkrevet,
                korrigertAv = sykepengesoknad.korrigertAv,
                korrigerer = sykepengesoknad.korrigerer,
                soknadPerioder = sykepengesoknad.soknadsperioder.map { SykepengesoknadDtoToSykepengesoknadMapper.konverter(it) },
                sporsmal = sykepengesoknad.sporsmal.map { SykepengesoknadDtoToSykepengesoknadMapper.konverter(it) }
        )
    }

    private fun <T> stream(list: List<T>?): Stream<T> {
        return list?.stream() ?: Stream.empty()
    }
}

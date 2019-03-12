package no.nav.syfo.kafka.mapper

import no.nav.syfo.domain.dto.SoknadPeriode
import no.nav.syfo.domain.dto.Sporsmal
import no.nav.syfo.domain.dto.Svar
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.sykepengesoknad.dto.SoknadsperiodeDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SporsmalDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SvarDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO

object SykepengesoknadDtoToSykepengesoknadMapper {

    private inline fun <T : Enum<*>, reified U : Enum<*>> T?.enumValueOrNull(): U? =
            U::class.java.enumConstants.firstOrNull { it.name == this?.name }

    private fun SvarDTO.toSvar(): Svar =
            Svar(this.verdi)


    private fun SporsmalDTO.toSporsmal(): Sporsmal =
            Sporsmal(
                    id = this.id,
                    tag = this.tag,
                    sporsmalstekst = this.sporsmalstekst,
                    undertekst = this.undertekst,
                    svartype = this.svartype.enumValueOrNull(),
                    min = this.min,
                    max = this.max,
                    kriterieForVisningAvUndersporsmal = this.kriteriumForVisningAvUndersporsmal.enumValueOrNull(),
                    svar = this.svar.map { it.toSvar() },
                    undersporsmal = this.undersporsmal.map { it.toSporsmal() }
            )


    private fun SoknadsperiodeDTO.toSoknadPeriode(): SoknadPeriode =
            SoknadPeriode(
                    fom = this.fom,
                    tom = this.tom,
                    grad = this.sykmeldingsgrad,
                    faktiskGrad = this.faktiskGrad)


    fun toSykepengesoknad(sykepengesoknad: SykepengesoknadDTO): Sykepengesoknad {
        return Sykepengesoknad(
                id = sykepengesoknad.id,
                sykmeldingId = sykepengesoknad.sykmeldingId,
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
                soknadPerioder = sykepengesoknad.soknadsperioder.map { it.toSoknadPeriode() },
                sporsmal = sykepengesoknad.sporsmal.map { it.toSporsmal() }
        )
    }
}

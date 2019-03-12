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

    private fun SporsmalDTO.toSporsmal(): Sporsmal =
            Sporsmal(
                    id = this.id,
                    tag = this.tag,
                    sporsmalstekst = this.sporsmalstekst,
                    undertekst = this.undertekst,
                    svartype = this.svartype.enumValueOrNull(),
                    min = this.min,
                    max = this.max,
                    kriterieForVisningAvUndersporsmal = this.kriterieForVisningAvUndersporsmal.enumValueOrNull(),
                    svar = this.svar.map { it.toSvar() },
                    undersporsmal = this.undersporsmal.map { it.toSporsmal() }
            )

    private fun SvarDTO.toSvar(): Svar =
            Svar(this.verdi)

    private fun SoknadPeriodeDTO.toSoknadPeriode(): SoknadPeriode =
            SoknadPeriode(
                    fom = this.fom,
                    tom = this.tom,
                    grad = this.grad)

    fun SoknadDTO.toSykepengesoknad(): Sykepengesoknad =
            Sykepengesoknad(
                    id = this.id,
                    sykmeldingId = this.sykmeldingId,
                    aktorId = this.aktorId,
                    soknadstype = this.soknadstype.enumValueOrNull(),
                    status = this.status,
                    fom = this.fom,
                    tom = this.tom,
                    opprettet = this.opprettetDato.atStartOfDay(),
                    sendtNav = this.innsendtDato?.atStartOfDay(),
                    arbeidsgiver = this.arbeidsgiver,
                    arbeidssituasjon = this.arbeidssituasjon.enumValueOrNull(),
                    startSykeforlop = this.startSykeforlop,
                    sykmeldingSkrevet = this.sykmeldingUtskrevet?.atStartOfDay(),
                    korrigertAv = this.korrigertAv,
                    korrigerer = this.korrigerer,
                    soknadPerioder = this.soknadPerioder.map { it.toSoknadPeriode() },
                    sporsmal = this.sporsmal.map { it.toSporsmal() })

}

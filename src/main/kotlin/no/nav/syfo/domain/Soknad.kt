package no.nav.syfo.domain

import no.nav.syfo.domain.dto.Arbeidssituasjon
import no.nav.syfo.domain.dto.SoknadPeriode
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Sporsmal
import no.nav.syfo.domain.dto.Sykepengesoknad
import java.time.LocalDate

data class Soknad(
        var aktorId: String? = null,
        var soknadsId: String? = null,
        var fnr: String? = null,
        var navn: String? = null,
        var soknadstype: Soknadstype? = null,
        var fom: LocalDate? = null,
        var tom: LocalDate? = null,
        var innsendtDato: LocalDate? = null,
        var sendtArbeidsgiver: LocalDate? = null,
        var startSykeforlop: LocalDate? = null,
        var sykmeldingUtskrevet: LocalDate? = null,
        var arbeidsgiver: String? = null,
        var korrigerer: String? = null,
        var korrigertAv: String? = null,
        var arbeidssituasjon: Arbeidssituasjon? = null,
        var soknadPerioder: List<SoknadPeriode>? = null,
        var sporsmal: List<Sporsmal>) {

    companion object {

        fun lagSoknad(sykepengesoknad: Sykepengesoknad, fnr: String, navn: String): Soknad =
                Soknad(
                        aktorId = sykepengesoknad.aktorId,
                        soknadsId = sykepengesoknad.id,
                        fnr = fnr,
                        navn = navn,
                        soknadstype = sykepengesoknad.soknadstype,
                        fom = sykepengesoknad.fom,
                        tom = sykepengesoknad.tom,
                        innsendtDato = sykepengesoknad.sendtNav?.toLocalDate(),
                        sendtArbeidsgiver = sykepengesoknad.sendtArbeidsgiver?.toLocalDate(),
                        startSykeforlop = sykepengesoknad.startSykeforlop,
                        sykmeldingUtskrevet = sykepengesoknad.sykmeldingSkrevet?.toLocalDate(),
                        arbeidsgiver = sykepengesoknad.arbeidsgiver,
                        korrigerer = sykepengesoknad.korrigerer,
                        korrigertAv = sykepengesoknad.korrigertAv,
                        arbeidssituasjon = sykepengesoknad.arbeidssituasjon,
                        soknadPerioder = sykepengesoknad.soknadPerioder,
                        sporsmal = endreRekkefolgePaSporsmalForPDF(sykepengesoknad.sporsmal)
                )


        private fun endreRekkefolgePaSporsmalForPDF(sporsmal: List<Sporsmal>): List<Sporsmal> {
            return sporsmal
                    .sortedBy { plasseringSporsmalPDF(it) }
        }

        private fun plasseringSporsmalPDF(sporsmal: Sporsmal): Int {
            return when (sporsmal.tag) {
                "BEKREFT_OPPLYSNINGER", "ANSVARSERKLARING" -> 1
                "VAER_KLAR_OVER_AT" -> 2
                else -> 0
            }
        }
    }
}

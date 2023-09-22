package no.nav.helse.flex.domain

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.domain.dto.Arbeidssituasjon
import no.nav.helse.flex.domain.dto.Avsendertype
import no.nav.helse.flex.domain.dto.Merknad
import no.nav.helse.flex.domain.dto.SoknadPeriode
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sporsmal
import no.nav.helse.flex.domain.dto.Svartype
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.util.OBJECT_MAPPER
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

data class Soknad(
    var aktorId: String? = null,
    var soknadsId: String? = null,
    var fnr: String? = null,
    var navn: String? = null,
    var tilNav: Boolean? = true,
    val soknadstype: Soknadstype,
    var fom: LocalDate? = null,
    var tom: LocalDate? = null,
    val opprettet: LocalDateTime,
    var innsendtTid: LocalDateTime? = null,
    var sendtArbeidsgiver: LocalDate? = null,
    var startSykeforlop: LocalDate? = null,
    var sykmeldingUtskrevet: LocalDate? = null,
    var arbeidsgiver: String? = null,
    var korrigerer: String? = null,
    var korrigertAv: String? = null,
    var arbeidssituasjon: Arbeidssituasjon? = null,
    var soknadPerioder: List<SoknadPeriode>? = null,
    var sporsmal: List<Sporsmal>,
    var avsendertype: Avsendertype? = null,
    var egenmeldtSykmelding: Boolean? = null,
    var orgNummer: String? = null,
    val merknaderFraSykmelding: List<Merknad>? = null,
    val kvitteringSum: Int? = null,
    val kvitteringer: List<PdfKvittering>? = null,
    val merknader: List<String>? = null,
    val utenlandskSykmelding: Boolean? = null,
    val egenmeldingsdagerFraSykmelding: List<LocalDate>? = null
) {

    companion object {
        fun lagSoknad(sykepengesoknad: Sykepengesoknad, fnr: String, navn: String): Soknad =
            Soknad(
                aktorId = sykepengesoknad.aktorId,
                soknadsId = sykepengesoknad.id,
                fnr = fnr,
                navn = navn,
                tilNav = true,
                soknadstype = sykepengesoknad.soknadstype,
                fom = sykepengesoknad.fom,
                tom = sykepengesoknad.tom,
                opprettet = sykepengesoknad.opprettet,
                innsendtTid = sykepengesoknad.sendtNav,
                sendtArbeidsgiver = sykepengesoknad.sendtArbeidsgiver?.toLocalDate(),
                startSykeforlop = sykepengesoknad.startSykeforlop,
                sykmeldingUtskrevet = sykepengesoknad.sykmeldingSkrevet?.toLocalDate(),
                arbeidsgiver = sykepengesoknad.arbeidsgiver,
                korrigerer = sykepengesoknad.korrigerer,
                korrigertAv = sykepengesoknad.korrigertAv,
                arbeidssituasjon = sykepengesoknad.arbeidssituasjon,
                soknadPerioder = sykepengesoknad.soknadPerioder,
                sporsmal = endreRekkefolgePaSporsmalForPDF(sykepengesoknad.sporsmal).filter { it.skalMedPaaPdf() },
                avsendertype = sykepengesoknad.avsendertype,
                egenmeldtSykmelding = sykepengesoknad.egenmeldtSykmelding,
                orgNummer = sykepengesoknad.orgNummer,
                merknaderFraSykmelding = sykepengesoknad.merknaderFraSykmelding,
                kvitteringSum = sykepengesoknad.hentKvitteringSum(),
                kvitteringer = sykepengesoknad.hentPdfKvitteringer(),
                merknader = sykepengesoknad.merknader,
                utenlandskSykmelding = sykepengesoknad.utenlandskSykmelding,
                egenmeldingsdagerFraSykmelding = sykepengesoknad.egenmeldingsdagerFraSykmelding?.sorted()
            )

        private fun endreRekkefolgePaSporsmalForPDF(sporsmal: List<Sporsmal>) =
            sporsmal.sortedBy { plasseringSporsmalPDF(it) }

        private fun plasseringSporsmalPDF(sporsmal: Sporsmal) = when (sporsmal.tag) {
            "BEKREFT_OPPLYSNINGER", "ANSVARSERKLARING" -> 1
            "VAER_KLAR_OVER_AT" -> 2
            else -> 0
        }
    }
}

private fun Sporsmal.skalMedPaaPdf(): Boolean = this.svartype != Svartype.KVITTERING

private fun Sykepengesoknad.hentKvitteringSum(): Int? {
    val hentKvitteringer = hentKvitteringer()
    return hentKvitteringer?.sumOf { it.belop }
}

private fun Sykepengesoknad.hentPdfKvitteringer(): List<PdfKvittering>? {
    return hentKvitteringer()
        ?.map {
            PdfKvittering(
                blobId = it.blobId,
                belop = it.belop,
                typeUtgift = it.typeUtgift,
                b64data = null
            )
        }
}

private fun Sykepengesoknad.hentKvitteringer(): List<Kvittering>? {
    if (this.soknadstype == Soknadstype.REISETILSKUDD || this.soknadstype == Soknadstype.GRADERT_REISETILSKUDD) {
        return this.sporsmal
            .filter { it.svartype == Svartype.KVITTERING }
            .flatMap { it.svar ?: emptyList() }
            .mapNotNull { it.verdi }
            .map { it.tilKvittering() }
    }
    return null
}

data class PdfKvittering(
    val b64data: String?,
    val blobId: String,
    val belop: Int,
    val typeUtgift: String
)

data class Kvittering(
    val blobId: String,
    val belop: Int, // Beløp i øre . 100kr = 10000
    val typeUtgift: String,
    val opprettet: Instant
)

private fun String.tilKvittering(): Kvittering {
    return OBJECT_MAPPER.readValue(this)
}

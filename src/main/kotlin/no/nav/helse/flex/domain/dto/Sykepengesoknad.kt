package no.nav.helse.flex.domain.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class Sykepengesoknad(
    val id: String,
    val sykmeldingId: String? = null,
    val aktorId: String,
    val fnr: String,
    val soknadstype: Soknadstype,
    val status: String,
    val fom: LocalDate? = null,
    val tom: LocalDate? = null,
    val opprettet: LocalDateTime,
    val sendtNav: LocalDateTime? = null,
    val sendtArbeidsgiver: LocalDateTime? = null,
    val startSykeforlop: LocalDate? = null,
    val sykmeldingSkrevet: LocalDateTime? = null,
    val arbeidsgiver: String? = null,
    val korrigerer: String? = null,
    val korrigertAv: String? = null,
    val arbeidssituasjon: Arbeidssituasjon? = null,
    val soknadPerioder: List<SoknadPeriode>? = null,
    val sporsmal: List<Sporsmal>,
    val avsendertype: Avsendertype? = null,
    val ettersending: Boolean? = null,
    val egenmeldtSykmelding: Boolean? = null,
    val harRedusertVenteperiode: Boolean = false,
    val orgNummer: String? = null,
    val merknaderFraSykmelding: List<Merknad>? = null,
    val merknader: List<String>? = null,
    val sendTilGosys: Boolean? = null,
    val utenlandskSykmelding: Boolean? = null,
    val egenmeldingsdagerFraSykmelding: List<LocalDate>?,
    val medlemskapVurdering: String? = null,
    val fiskerBlad: String? = null,
)

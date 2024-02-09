package no.nav.helse.flex.service

import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sykepengesoknad

const val FORKORTET_VENTETID = "ae0247"
const val TILBAKEDATERING = "ae0239"
const val OVERGANGSSAK_FRA_SPEIL = "ab0455"
const val UTLAND = "ae0106"
const val MEDLEMSKAP = "ab0269"
const val SYKEPENGER_UNDER_UTENLANDSOPPHOLD = "ab0314"
const val ENKELTSTAENDE_BEHANDLINGSDAGER = "ab0351"
const val SYKEPENGER_FOR_ARBEIDSLEDIG = "ab0426"
const val REISETILSKUDD = "ab0237"
const val SYKEPENGER = "ab0061"

fun finnBehandlingstemaOgType(
    soknad: Sykepengesoknad,
    harRedusertVenteperiode: Boolean,
    speilRelatert: Boolean,
    medlemskapVurdering: String?,
): BehandlingstemaOgType {
    if (harRedusertVenteperiode) {
        return behandlingstype(FORKORTET_VENTETID)
    }
    if (soknad.gjelderTilbakedatering()) {
        return behandlingstype(TILBAKEDATERING)
    }
    if (speilRelatert) {
        return behandlingstema(OVERGANGSSAK_FRA_SPEIL)
    }
    if (soknad.utenlandskSykmelding == true) {
        return behandlingstype(UTLAND)
    }
    if (medlemskapVurdering in listOf("NEI", "UAVKLART")) {
        return behandlingstema(MEDLEMSKAP)
    }
    return behandlingstema(
        when (soknad.soknadstype) {
            Soknadstype.OPPHOLD_UTLAND -> SYKEPENGER_UNDER_UTENLANDSOPPHOLD
            Soknadstype.BEHANDLINGSDAGER -> ENKELTSTAENDE_BEHANDLINGSDAGER
            Soknadstype.ARBEIDSLEDIG -> SYKEPENGER_FOR_ARBEIDSLEDIG
            Soknadstype.REISETILSKUDD, Soknadstype.GRADERT_REISETILSKUDD -> REISETILSKUDD
            else -> SYKEPENGER
        },
    )
}

fun behandlingstema(tema: String): BehandlingstemaOgType {
    return BehandlingstemaOgType(behandlingstema = tema, behandlingstype = null)
}

fun behandlingstype(type: String): BehandlingstemaOgType {
    return BehandlingstemaOgType(behandlingstema = null, behandlingstype = type)
}

data class BehandlingstemaOgType(
    val behandlingstema: String?,
    val behandlingstype: String?,
)

private fun Sykepengesoknad.gjelderTilbakedatering(): Boolean {
    return this.merknaderFraSykmelding?.any {
        listOf(
            "UGYLDIG_TILBAKEDATERING",
            "TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER",
            "TILBAKEDATERT_PAPIRSYKMELDING",
            "UNDER_BEHANDLING",
        ).contains(it.type)
    } ?: false
}

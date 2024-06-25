package no.nav.helse.flex.service

import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sykepengesoknad

const val BEHANDLINGSTEMA_FORKORTET_VENTETID = "ae0247"
const val BEHANDLINGSTEMA_TILBAKEDATERING = "ae0239"
const val BEHANDLINGSTEMA_OVERGANGSSAK_FRA_SPEIL = "ab0455"
const val BEHANDLINGSTEMA_UTLAND = "ae0106"
const val BEHANDLINGSTEMA_MEDLEMSKAP = "ab0269"
const val BEHANDLINGSTEMA_SYKEPENGER_UNDER_UTENLANDSOPPHOLD = "ab0314"
const val BEHANDLINGSTEMA_ENKELTSTAENDE_BEHANDLINGSDAGER = "ab0351"
const val BEHANDLINGSTEMA_SYKEPENGER_FOR_ARBEIDSLEDIG = "ab0426"
const val BEHANDLINGSTEMA_REISETILSKUDD = "ab0237"
const val BEHANDLINGSTEMA_SYKEPENGER = "ab0061"

fun finnBehandlingstemaOgType(
    soknad: Sykepengesoknad,
    harRedusertVenteperiode: Boolean,
    speilRelatert: Boolean,
    medlemskapVurdering: String?,
): BehandlingstemaOgType {
    if (harRedusertVenteperiode) {
        return behandlingstype(BEHANDLINGSTEMA_FORKORTET_VENTETID)
    }
    if (soknad.gjelderTilbakedatering()) {
        return behandlingstype(BEHANDLINGSTEMA_TILBAKEDATERING)
    }
    if (speilRelatert) {
        return behandlingstema(BEHANDLINGSTEMA_OVERGANGSSAK_FRA_SPEIL)
    }
    if (soknad.utenlandskSykmelding == true) {
        return behandlingstype(BEHANDLINGSTEMA_UTLAND)
    }
    if (medlemskapVurdering in listOf("UAVKLART", "NEI")) {
        return behandlingstema(BEHANDLINGSTEMA_MEDLEMSKAP)
    }
    return behandlingstema(
        when (soknad.soknadstype) {
            Soknadstype.OPPHOLD_UTLAND -> BEHANDLINGSTEMA_SYKEPENGER_UNDER_UTENLANDSOPPHOLD
            Soknadstype.BEHANDLINGSDAGER -> BEHANDLINGSTEMA_ENKELTSTAENDE_BEHANDLINGSDAGER
            Soknadstype.ARBEIDSLEDIG -> BEHANDLINGSTEMA_SYKEPENGER_FOR_ARBEIDSLEDIG
            Soknadstype.REISETILSKUDD, Soknadstype.GRADERT_REISETILSKUDD -> BEHANDLINGSTEMA_REISETILSKUDD
            else -> BEHANDLINGSTEMA_SYKEPENGER
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
            "DELVIS_GODKJENT",
        ).contains(it.type)
    } ?: false
}

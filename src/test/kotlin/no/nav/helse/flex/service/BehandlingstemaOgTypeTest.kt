package no.nav.helse.flex.service

import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class BehandlingstemaOgTypeTest {
    @Test
    fun arbeidsledig() {
        finnBehandlingstemaOgType(
            soknad = soknad(soknadstype = Soknadstype.ARBEIDSLEDIG),
            harRedusertVenteperiode = false,
            speilRelatert = false,
            medlemskapVurdering = null,
        ).behandlingstema `should be equal to` "ab0426"
    }

    @Test
    fun oppholdUtland() {
        finnBehandlingstemaOgType(
            soknad = soknad(soknadstype = Soknadstype.OPPHOLD_UTLAND),
            harRedusertVenteperiode = false,
            speilRelatert = false,
            medlemskapVurdering = null,
        ).behandlingstema `should be equal to` "ab0314"
    }

    @Test
    fun arbeidstaker() {
        finnBehandlingstemaOgType(
            soknad = soknad(),
            harRedusertVenteperiode = false,
            speilRelatert = false,
            medlemskapVurdering = null,
        ).behandlingstema `should be equal to` "ab0061"
    }

    @Test
    fun behandlingsdager() {
        finnBehandlingstemaOgType(
            soknad = soknad(Soknadstype.BEHANDLINGSDAGER),
            harRedusertVenteperiode = false,
            speilRelatert = false,
            medlemskapVurdering = null,
        ).behandlingstema `should be equal to` "ab0351"
    }

    @Test
    fun redusertVenteperiode() {
        finnBehandlingstemaOgType(
            soknad = soknad(Soknadstype.SELVSTENDIGE_OG_FRILANSERE),
            harRedusertVenteperiode = true,
            speilRelatert = false,
            medlemskapVurdering = null,
        ).behandlingstype `should be equal to` "ae0247"
    }

    private fun soknad(soknadstype: Soknadstype = Soknadstype.ARBEIDSTAKERE): Sykepengesoknad =
        Sykepengesoknad(
            id = UUID.randomUUID().toString(),
            aktorId = UUID.randomUUID().toString(),
            egenmeldingsdagerFraSykmelding = emptyList(),
            fnr = "fnr",
            soknadstype = soknadstype,
            opprettet = LocalDateTime.now(),
            status = "SENDT",
            sporsmal = emptyList(),
        )
}

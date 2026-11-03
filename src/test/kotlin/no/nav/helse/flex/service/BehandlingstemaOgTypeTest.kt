package no.nav.helse.flex.service

import io.kotest.matchers.shouldBe
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class BehandlingstemaOgTypeTest {
    @Test
    fun arbeidsledig() {
        finnBehandlingstemaOgType(
            soknad = soknad(soknadstype = Soknadstype.ARBEIDSLEDIG),
            speilRelatert = false,
            medlemskapVurdering = null,
        ).behandlingstema shouldBe "ab0426"
    }

    @Test
    fun oppholdUtland() {
        finnBehandlingstemaOgType(
            soknad = soknad(soknadstype = Soknadstype.OPPHOLD_UTLAND),
            speilRelatert = false,
            medlemskapVurdering = null,
        ).behandlingstema shouldBe "ab0314"
    }

    @Test
    fun arbeidstaker() {
        finnBehandlingstemaOgType(
            soknad = soknad(),
            speilRelatert = false,
            medlemskapVurdering = null,
        ).behandlingstema shouldBe "ab0061"
    }

    @Test
    fun behandlingsdager() {
        finnBehandlingstemaOgType(
            soknad = soknad(Soknadstype.BEHANDLINGSDAGER),
            speilRelatert = false,
            medlemskapVurdering = null,
        ).behandlingstema shouldBe "ab0351"
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
            meldingTilNavDagerFraSykmelding = emptyList(),
        )
}

package no.nav.helse.flex.arkivering

import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Soknadstype.ARBEIDSLEDIG
import no.nav.helse.flex.domain.dto.Sporsmal
import no.nav.helse.flex.domain.dto.Svar
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SorterViktigeSporsmalTest {

    @Test
    fun testSortering() {
        val soknad = Soknad(
            opprettet = LocalDateTime.now(),
            soknadstype = ARBEIDSLEDIG,
            sporsmal = listOf(
                sporsmal("NEI", "FERIE"),
                sporsmal("JA", "FRISKMELDT"),
                sporsmal("JA", "ANDRE_INNTEKTSKILDER"),
                sporsmal("NEI", "ARBEID_UTENFOR_NORGE")
            )
        )
        soknad.sorterViktigeSporsmalFørst().sporsmal.map { it.tag } shouldBeEqualTo listOf(
            "ANDRE_INNTEKTSKILDER",
            "FERIE",
            "FRISKMELDT",
            "ARBEID_UTENFOR_NORGE"
        )
    }

    @Test
    fun testSorteringFriskmeldtNei() {
        val soknad = Soknad(
            opprettet = LocalDateTime.now(),
            soknadstype = ARBEIDSLEDIG,
            sporsmal = listOf(
                sporsmal("NEI", "FERIE"),
                sporsmal("NEI", "FRISKMELDT"),
                sporsmal("JA", "ANDRE_INNTEKTSKILDER"),
                sporsmal("NEI", "ARBEID_UTENFOR_NORGE")
            )
        )
        soknad.sorterViktigeSporsmalFørst().sporsmal.map { it.tag } shouldBeEqualTo listOf(
            "FRISKMELDT",
            "ANDRE_INNTEKTSKILDER",
            "FERIE",
            "ARBEID_UTENFOR_NORGE"
        )
    }

    fun sporsmal(svar: String, tag: String) = Sporsmal(
        id = "a",
        tag = tag,
        svar = listOf(
            Svar(verdi = svar)
        )
    )
}

package no.nav.helse.flex.client.pdl

import io.kotest.matchers.shouldBe
import no.nav.helse.flex.FellesTestOppsett
import no.nav.helse.flex.service.IdentService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PdlTest : FellesTestOppsett() {
    @Autowired
    private lateinit var identService: IdentService

    val ident = "11111111111"

    @Test
    fun `Henter fnr i fra pdl`() {
        val fnr = identService.hentFnrForAktorId(ident)
        fnr.shouldBe(ident)
    }

    @Test
    fun `Henter aktorId i fra pdl`() {
        val aktorId = identService.hentAktorIdForFnr(ident)
        aktorId.shouldBe(ident + "00")
    }
}

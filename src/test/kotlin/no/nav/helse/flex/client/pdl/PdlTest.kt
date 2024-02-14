package no.nav.helse.flex.client.pdl

import no.nav.helse.flex.FellesTestOppsett
import no.nav.helse.flex.service.IdentService
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PdlTest : FellesTestOppsett() {
    @Autowired
    private lateinit var identService: IdentService

    val ident = "11111111111"

    @Test
    fun `Henter fnr i fra pdl`() {
        val fnr = identService.hentFnrForAktorId(ident)
        fnr.shouldBeEqualTo(ident)
    }

    @Test
    fun `Henter aktorId i fra pdl`() {
        val aktorId = identService.hentAktorIdForFnr(ident)
        aktorId.shouldBeEqualTo(ident + "00")
    }
}

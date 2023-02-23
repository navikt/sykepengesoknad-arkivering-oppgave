package no.nav.helse.flex.client.pdl

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.service.IdentService
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext

@DirtiesContext
class PdlTest : FellesTestoppsett() {

    @MockBean
    private lateinit var pdlClient: PdlClient

    @Autowired
    private lateinit var identService: IdentService

    private val fnr = "12345678901"
    private val aktorId = "aktorid123"
    private val hentIdenterResponseData = HentIdenterResponseData(
        hentIdenter = HentIdenter(
            listOf(
                PdlIdent(gruppe = AKTORID, ident = aktorId),
                PdlIdent(gruppe = FOLKEREGISTERIDENT, ident = fnr)
            )
        )
    )

    @Test
    fun `Henter fnr i fra pdl`() {
        whenever(pdlClient.hentIdenter(any())).thenReturn(hentIdenterResponseData)
        identService.hentFnrForAktorId(aktorId) `should be equal to` fnr
    }

    @Test
    fun `Henter aktorId i fra pdl`() {
        whenever(pdlClient.hentIdenter(any())).thenReturn(hentIdenterResponseData)
        identService.hentAktorIdForFnr(fnr) `should be equal to` aktorId
    }
}

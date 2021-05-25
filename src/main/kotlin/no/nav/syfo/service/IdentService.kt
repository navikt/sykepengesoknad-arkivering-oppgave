package no.nav.syfo.service

import no.nav.syfo.client.pdl.AKTORID
import no.nav.syfo.client.pdl.FOLKEREGISTERIDENT
import no.nav.syfo.client.pdl.HentIdenterResponseData
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.logger
import org.springframework.stereotype.Component

@Component
class IdentService(private val pdlClient: PdlClient) {

    val log = logger()

    fun hentAktorIdForFnr(fnr: String): String {
        return pdlClient.hentIdenter(fnr).aktorId()
    }

    fun hentFnrForAktorId(aktorId: String): String {
        return pdlClient.hentIdenter(aktorId).fnr()
    }

    private fun HentIdenterResponseData.aktorId(): String {
        return this.hentIdenter?.identer?.find { it.gruppe == AKTORID }?.ident
            ?: throw RuntimeException("Kunne ikke finne aktørid i pdl response")
    }

    private fun HentIdenterResponseData.fnr(): String {
        return this.hentIdenter?.identer?.find { it.gruppe == FOLKEREGISTERIDENT }?.ident
            ?: throw RuntimeException("Kunne ikke finne fnr i pdl response")
    }
}

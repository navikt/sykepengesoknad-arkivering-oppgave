package no.nav.helse.flex.service

import no.nav.helse.flex.client.pdl.*
import no.nav.helse.flex.logger
import org.springframework.stereotype.Component

@Component
class IdentService(
    private val pdlClient: PdlClient,
) {
    val log = logger()

    fun hentAktorIdForFnr(fnr: String): String = pdlClient.hentIdenter(fnr).aktorId()

    fun hentFnrForAktorId(aktorId: String): String = pdlClient.hentIdenter(aktorId).fnr()
}

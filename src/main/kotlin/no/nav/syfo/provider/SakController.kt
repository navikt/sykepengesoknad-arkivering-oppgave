package no.nav.syfo.provider

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.syfo.consumer.repository.InnsendingDAO
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/"])
class SakController(
        val innsendingDAO: InnsendingDAO) {

    @ResponseBody
    @ProtectedWithClaims(issuer = "azuread")
    @GetMapping("/{aktorId}/sisteSak")
    fun finnSisteSak(@PathVariable aktorId: String): SisteSakRespons {
        return SisteSakRespons(innsendingDAO.finnSisteSak(aktorId))
    }

    data class SisteSakRespons(
            val sisteSak: String?
    )
}

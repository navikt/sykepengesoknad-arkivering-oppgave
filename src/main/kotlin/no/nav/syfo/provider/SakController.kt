package no.nav.syfo.provider

import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.repository.TidligereInnsending
import no.nav.syfo.log
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping(value = ["/"])
class SakController(
    val innsendingDAO: InnsendingDAO
) {

    val log = log()

    @ResponseBody
    @ProtectedWithClaims(issuer = "azuread")
    @GetMapping("/{aktorId}/sisteSak")
    fun finnSisteSak(
        @PathVariable aktorId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fom: LocalDate?,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) tom: LocalDate?
    ): SisteSakRespons {
        validerInput(fom, tom)


        log.warn("Noen kaller sisteSak")

        return SisteSakRespons(
            innsendingDAO.finnTidligereInnsendinger(aktorId)
                .filter { overlapperMedSoknad(it, fom, tom) }
                .sortedByDescending { it.soknadFom }
                .firstOrNull()
                ?.saksId
        )
    }

    private fun validerInput(fom: LocalDate?, tom: LocalDate?){
        if (fom != null && tom != null && fom.isAfter(tom)) {
            log.error("FOM må være før eller lik TOM")
            throw IllegalArgumentException("FOM må være før eller lik TOM")
        }
    }

    data class SisteSakRespons(
        val sisteSak: String?
    )
}

fun overlapperMedSoknad(innsending: TidligereInnsending, fom: LocalDate?, tom: LocalDate?): Boolean {
    return if (fom != null && tom != null) {
        val range = innsending.soknadFom..innsending.soknadTom
        fom in range || tom in range
    } else {
        true
    }
}

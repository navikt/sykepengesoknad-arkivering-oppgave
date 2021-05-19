package no.nav.syfo.provider

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.syfo.AAD
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.repository.TidligereInnsending
import no.nav.syfo.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDate

@RestController
@RequestMapping(value = ["/"])
class SakController(
    val innsendingDAO: InnsendingDAO,
    @Value("\${aad_syfoinntektsmelding_clientid_username}") val syfoinntektsmeldingClientId: String,
    private val contextHolder: TokenValidationContextHolder,
) {

    private val log = logger()

    @ResponseBody
    @ProtectedWithClaims(issuer = AAD)
    @GetMapping("/{aktorId}/sisteSak")
    fun finnSisteSak(
        @PathVariable aktorId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fom: LocalDate?,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) tom: LocalDate?
    ): SisteSakRespons {

        val subjekt = appIdFraToken()
        if (subjekt != syfoinntektsmeldingClientId) {
            log.warn("$subjekt har ikke til dette APIet")
            throw HttpClientErrorException(HttpStatus.FORBIDDEN)
        }

        validerInput(fom, tom)

        return SisteSakRespons(
            innsendingDAO.finnTidligereInnsendinger(aktorId)
                .filter { overlapperMedSoknad(it, fom, tom) }
                .sortedByDescending { it.soknadFom }
                .firstOrNull()
                ?.saksId
        )
    }

    private fun validerInput(fom: LocalDate?, tom: LocalDate?) {
        if (fom != null && tom != null && fom.isAfter(tom)) {
            log.error("FOM må være før eller lik TOM")
            throw IllegalArgumentException("FOM må være før eller lik TOM")
        }
    }

    data class SisteSakRespons(
        val sisteSak: String?
    )

    private fun appIdFraToken(): String {
        val context = contextHolder.tokenValidationContext
        return context.getClaims(AAD).getStringClaim("appid") ?: run {
            log.warn("Ingen appid claim i token")
            throw HttpClientErrorException(HttpStatus.UNAUTHORIZED)
        }
    }
}

fun overlapperMedSoknad(innsending: TidligereInnsending, fom: LocalDate?, tom: LocalDate?): Boolean {
    return if (fom != null && tom != null) {
        val range = innsending.soknadFom..innsending.soknadTom
        fom in range || tom in range
    } else {
        true
    }
}

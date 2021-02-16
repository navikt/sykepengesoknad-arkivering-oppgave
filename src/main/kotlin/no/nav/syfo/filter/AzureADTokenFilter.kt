package no.nav.syfo.filter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.security.oidc.api.ProtectedWithClaims
import no.nav.security.spring.oidc.validation.interceptor.OIDCUnauthorizedException
import no.nav.syfo.AZUREAD
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.jwt.JwtHelper
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Order(Ordered.LOWEST_PRECEDENCE)
class AzureADTokenFilter(val syfogsakClientId: String, val authorizedConsumerClientIds: List<String>, val issuer: String) : HandlerInterceptor {

    val mapper = ObjectMapper()
        .registerModule(KotlinModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {

            (finnProtectedWithClaimsPaMetode(handler) ?: finnProtectedWithClaimsPaKlasse(handler))
                ?.issuer
                ?.takeIf { it.equals(AZUREAD) }
                ?.let {
                    val auth: String? = request.getHeader("Authorization")?.substringAfter("Bearer")?.trim()
                    val claimsJson = auth
                        ?.let { JwtHelper.decode(it) }
                        ?.claims
                        ?: throw OIDCUnauthorizedException("Ugyldige credentials")

                    val claims = mapper.readValue(claimsJson, Claims::class.java)
                    if (!(
                        claims.appid in authorizedConsumerClientIds &&
                            claims.aud == syfogsakClientId &&
                            claims.iss == issuer
                        )
                    ) {
                        throw OIDCUnauthorizedException("Ugyldige credentials")
                    }
                }
        }
        return super.preHandle(request, response, handler)
    }

    private fun finnProtectedWithClaimsPaMetode(handler: HandlerMethod) =
        handler.getMethodAnnotation(ProtectedWithClaims::class.java)

    fun finnProtectedWithClaimsPaKlasse(handlerMethod: HandlerMethod): ProtectedWithClaims? {
        val method = handlerMethod.method
        val declaringClass = method.declaringClass

        if (declaringClass.isAnnotationPresent(ProtectedWithClaims::class.java)) {
            return declaringClass.getAnnotation(ProtectedWithClaims::class.java)
        }

        return null
    }
}

data class Claims(
    val aud: String,
    val appid: String,
    val iss: String // Azure AD NAV
)

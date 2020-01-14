package no.nav.syfo.config

import no.nav.syfo.filter.AzureADTokenFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebMVCConfig(
        @Value("\${aad_syfoinntektsmelding_clientid_username}") val syfoinntektsmeldingClientId: String,
        @Value("\${no.nav.security.oidc.issuer.azuread.acceptedaudience}") val syfogsakClientId: String,
        @Value("\${syfo.jwt.issuer.url}") val issuer: String) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(AzureADTokenFilter(syfogsakClientId, listOf(syfoinntektsmeldingClientId), issuer))
    }
}

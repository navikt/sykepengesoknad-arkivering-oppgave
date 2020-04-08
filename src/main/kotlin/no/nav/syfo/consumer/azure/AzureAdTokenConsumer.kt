package no.nav.syfo.consumer.azure

import no.nav.syfo.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.Instant
import java.util.*

@Component
class AzureAdTokenConsumer(private val restTemplateMedProxy: RestTemplate,
                           @param:Value("\${aad_accesstoken_url}") private val url: String,
                           @param:Value("\${aad_syfogsak_client_id}") private val clientId: String,
                           @param:Value("\${aad_syfogsak_client_secret}") private val clientSecret: String) {
    private val azureAdTokenMap = HashMap<String, AzureAdToken>()

    val log = log()

    fun getAccessToken(resource: String): String {
        val omToMinutter = Instant.now().plusSeconds(120L)
        synchronized(this) {
            val azureAdToken = azureAdTokenMap[resource]
            if (azureAdToken?.expires_on == null || azureAdToken.expires_on.isBefore(omToMinutter)) {
                log.info("Henter nytt token fra Azure AD for ressurs {}", resource)
                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

                val body = LinkedMultiValueMap<String, String>()
                body.add("client_id", clientId)
                body.add("resource", resource)
                body.add("grant_type", "client_credentials")
                body.add("client_secret", clientSecret)

                val uriString = UriComponentsBuilder.fromHttpUrl(url).toUriString()

                val result = restTemplateMedProxy.exchange(uriString, POST, HttpEntity<MultiValueMap<String, String>>(body, headers), AzureAdToken::class.java)

                if (result.statusCode != OK) {
                    throw RuntimeException("Henting av token fra Azure AD feiler med HTTP-" + result.statusCode)
                }
                if (result.body == null) {
                    throw RuntimeException("Henting av token fra Azure AD feiler fordi body er null")
                }
                azureAdTokenMap[resource] = result.body!!

            }
        }
        return azureAdTokenMap[resource]?.access_token ?: throw RuntimeException("Ingen azure ad token tilgjengelig")

    }
}

class AzureAdToken(
        val access_token: String,
        val expires_on: Instant? = null
)

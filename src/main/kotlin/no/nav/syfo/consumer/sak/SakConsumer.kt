package no.nav.syfo.consumer.sak

import no.nav.syfo.config.ApplicationConfig.CALL_ID
import no.nav.syfo.consumer.token.TokenConsumer
import no.nav.syfo.log
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class SakConsumer(private val tokenConsumer: TokenConsumer,
                  @Value("\${srvsyfogsak.username}") private val username: String,
                  @Value("\${sak.saker.url}") private val url: String,
                  private val restTemplate: RestTemplate) {

    val log = log()

    fun opprettSak(aktorId: String): String {
        val uriString = UriComponentsBuilder.fromHttpUrl(url).toUriString()

        try {
            val result = restTemplate.exchange(uriString, HttpMethod.POST, HttpEntity(lagRequestBody(aktorId), lagRequestHeaders()), SakJson::class.java)

            if (result.statusCode != HttpStatus.OK) {
                if (result.statusCode == HttpStatus.CONFLICT) {
                    log.error("Tilsvarende sak finnes fra før for aktør $aktorId")
                }
                val message = "Oppretting av sak for aktør $aktorId feiler med HTTP-" + result.statusCode
                log.error(message)
                throw RuntimeException(message)
            }

            return result
                    .let {
                        it.body ?: throw RuntimeException("Sak-respons mangler ved oppretting av sak for $aktorId - skal ikke kunne skje!")
                    }
                    .id.toString()
        } catch (e: HttpClientErrorException) {
            log.error("Feil ved oppretting av sak for aktør $aktorId", e)
            throw RuntimeException(e)
        }
    }

    fun lagRequestHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("Authorization", "Bearer " + tokenConsumer.token.access_token)
        headers.set("Nav-Call-Id", MDC.get(CALL_ID))
        headers.set("X-Correlation-ID", MDC.get(CALL_ID))
        headers.set("Nav-Consumer-Id", username)
        return headers
    }

    fun lagRequestBody(aktorId: String): LinkedMultiValueMap<String, String> {
        val body = LinkedMultiValueMap<String, String>()
        body.add("tema", "SYK")
        body.add("applikasjon", "FS22")
        body.add("aktoerId", aktorId)
        return body
    }
}

data class SakJson(
        val id: Int,
        val tema: String?,
        val applikasjon: String?,
        val aktoerId: String?,
        val orgnr: String?,
        val fagsakNr: String?,
        val opprettetAv: String?,
        val opprettetTidspunkt: String?
)

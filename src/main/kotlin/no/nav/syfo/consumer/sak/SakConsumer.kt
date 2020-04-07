package no.nav.syfo.consumer.sak

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.syfo.consumer.token.TokenConsumer
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.log
import no.nav.syfo.util.callId
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
@Component
class SakConsumer(
    private val tokenConsumer: TokenConsumer,
    @Value("\${srvsyfogsak.username}") private val username: String,
    @Value("\${sak.saker.url}") private val url: String,
    private val restTemplate: RestTemplate
) {

    val log = log()


    fun opprettSak(aktorId: String): String {

        try {
            val uriString = UriComponentsBuilder.fromHttpUrl(url).toUriString()
            val result = restTemplate.exchange(uriString, HttpMethod.POST, HttpEntity(lagRequestBody(aktorId), lagRequestHeaders()), SakResponse::class.java)

            if (result.statusCode != HttpStatus.CREATED) {
                if (result.statusCode == HttpStatus.CONFLICT) {
                    log.error("Tilsvarende sak finnes fra før for aktør $aktorId")
                }
                val message = "Oppretting av sak for aktør $aktorId feiler med HTTP-" + result.statusCode
                log.error(message)
                throw RuntimeException(message)
            }

            return result
                .let {
                    it.body
                        ?: throw RuntimeException("Sak-respons mangler ved oppretting av sak for $aktorId - skal ikke kunne skje!")
                }
                .id.toString()
        } catch (e: HttpClientErrorException) {
            log.error("Feil ved oppretting av sak for aktør $aktorId", e)
            throw RuntimeException(e)
        }
    }

    fun lagRequestHeaders(): HttpHeaders = HttpHeaders().apply {
        this.contentType = MediaType.APPLICATION_JSON
        this.set("Authorization", "Bearer " + tokenConsumer.token.access_token)
        this.set("Nav-Call-Id", callId())
        this.set("X-Correlation-ID", callId())
        this.set("Nav-Consumer-Id", username)
    }

    fun lagRequestBody(aktorId: String): SakRequest =
        SakRequest("SYK", "FS22", aktorId)
}


data class SakRequest(
    val tema: String,
    val applikasjon: String,
    val aktoerId: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SakResponse(
    val id: Int,
    val tema: String?,
    val applikasjon: String?,
    val aktoerId: String?,
    val orgnr: String?,
    val fagsakNr: String?,
    val opprettetAv: String?,
    val opprettetTidspunkt: String?
)

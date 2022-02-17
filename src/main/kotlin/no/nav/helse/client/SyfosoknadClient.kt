package no.nav.helse.client

import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.helse.logger
import no.nav.helse.util.callId
import no.nav.syfo.kafka.NAV_CALLID
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class SyfosoknadClient(
    @Value("\${FLEX_FSS_PROXY_URL}")
    private val url: String,
    private val syfosoknadRestTemplate: RestTemplate
) {

    private val log = logger()

    fun hentSoknad(soknadId: String): SykepengesoknadDTO {
        try {
            val uriBuilder = UriComponentsBuilder.fromHttpUrl("$url/api/v3/soknader/$soknadId/kafkaformat")

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.set(NAV_CALLID, callId())

            val result = syfosoknadRestTemplate
                .exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    SykepengesoknadDTO::class.java
                )

            if (result.statusCode != OK) {
                val message = "Kall mot syfosoknad feiler med HTTP-" + result.statusCode
                log.error(message)
                throw RuntimeException(message)
            }

            result.body?.let { return it }

            val message = "Kall mot syfosoknad returnerer ikke data"
            log.error(message)
            throw RuntimeException(message)
        } catch (ex: HttpClientErrorException.NotFound) {
            throw SøknadIkkeFunnetException("Fant ikke søknad: $soknadId")
        }
    }
}

class SøknadIkkeFunnetException(msg: String) : RuntimeException(msg)
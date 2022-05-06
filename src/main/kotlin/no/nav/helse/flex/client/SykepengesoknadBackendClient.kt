package no.nav.helse.flex.client

import no.nav.helse.flex.logger
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.helse.flex.util.callId
import no.nav.syfo.kafka.NAV_CALLID
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
class SykepengesoknadBackendClient(
    private val sykepengesoknadBackendRestTemplate: RestTemplate
) {

    private val log = logger()

    fun hentSoknad(soknadId: String): SykepengesoknadDTO {
        try {
            val uriBuilder =
                UriComponentsBuilder.fromHttpUrl("http://sykepengesoknad-backend/api/v3/soknader/$soknadId/kafkaformat")

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.set(NAV_CALLID, callId())

            val result = sykepengesoknadBackendRestTemplate
                .exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    SykepengesoknadDTO::class.java
                )

            if (result.statusCode != OK) {
                val message = "Kall mot sykepengesoknad-backend feiler med HTTP-" + result.statusCode
                log.error(message)
                throw RuntimeException(message)
            }

            result.body?.let { return it }

            val message = "Kall mot sykepengesoknad-backend returnerer ikke data"
            log.error(message)
            throw RuntimeException(message)
        } catch (ex: HttpClientErrorException.NotFound) {
            throw SøknadIkkeFunnetException("Fant ikke søknad: $soknadId")
        }
    }
}

class SøknadIkkeFunnetException(msg: String) : RuntimeException(msg)

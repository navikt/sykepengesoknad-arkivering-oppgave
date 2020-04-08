package no.nav.syfo.consumer.syfosoknad

import no.nav.syfo.consumer.azure.AzureAdTokenConsumer
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.felles.SykepengesoknadDTO

import no.nav.syfo.log
import no.nav.syfo.util.callId
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class SyfosoknadConsumer(private val restTemplate: RestTemplate,
                         private val azureAdTokenConsumer: AzureAdTokenConsumer,
                         @param:Value("\${aad_syfosoknad_clientid}") private val resource: String,
                         @param:Value("\${syfosoknad_url}") private val url: String) {

    val log = log()

    fun hentSoknad(soknadId: String): SykepengesoknadDTO {
        val uriBuilder = UriComponentsBuilder.fromHttpUrl("$url/api/soknader/$soknadId/kafkaformat")


        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer ${azureAdTokenConsumer.getAccessToken(resource)}")
        headers.set(NAV_CALLID, callId())

        val result = restTemplate
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
    }
}

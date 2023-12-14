package no.nav.helse.flex.client.pdl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.graphql.GraphQLRequest
import no.nav.helse.flex.graphql.GraphQLResponse
import no.nav.helse.flex.objectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.*

private const val TEMA = "Tema"
private const val TEMA_SYK = "SYK"
private const val IDENT = "ident"
private const val BEHANDLINGSNUMMER_KEY = "Behandlingsnummer"
private const val BEHANDLINGSNUMMER_VALUE = "B128"

@Component
class PdlClient(
    @Value("\${PDL_URL}")
    private val pdlApiUrl: String,
    private val pdlRestTemplate: RestTemplate,
) {
    @Retryable(exclude = [FunctionalPdlError::class])
    fun hentIdenter(ident: String): List<PdlIdent> {
        val graphQLRequest =
            GraphQLRequest(
                query = HENT_IDENTER_QUERY,
                variables = Collections.singletonMap(IDENT, ident),
            )

        val responseEntity =
            pdlRestTemplate.exchange(
                "$pdlApiUrl/graphql",
                HttpMethod.POST,
                HttpEntity(requestToJson(graphQLRequest), createHeaders()),
                String::class.java,
            )

        if (responseEntity.statusCode != HttpStatus.OK) {
            throw RuntimeException("PDL svarer med status ${responseEntity.statusCode} - ${responseEntity.body}")
        }

        val parsedResponse =
            responseEntity.body?.let { objectMapper.readValue<GraphQLResponse<HentIdenterResponseData>>(it) }

        val identer =
            parsedResponse?.data?.let {
                it.hentIdenter?.identer
            } ?: throw FunctionalPdlError("Fant ikke person, ingen body eller data. ${parsedResponse?.hentErrors()}")

        return identer
    }

    @Retryable(exclude = [FunctionalPdlError::class])
    fun hentFormattertNavn(fnr: String): String {
        val graphQLRequest =
            GraphQLRequest(
                query = HENT_NAVN_QUERY,
                variables = Collections.singletonMap(IDENT, fnr),
            )

        val responseEntity =
            pdlRestTemplate.exchange(
                "$pdlApiUrl/graphql",
                HttpMethod.POST,
                HttpEntity(requestToJson(graphQLRequest), createHeaders()),
                String::class.java,
            )

        if (responseEntity.statusCode != HttpStatus.OK) {
            throw RuntimeException("PDL svarer med status ${responseEntity.statusCode} - ${responseEntity.body}")
        }

        val parsedResponse =
            responseEntity.body?.let { objectMapper.readValue<GraphQLResponse<HentNavnResponseData>>(it) }

        val navn =
            parsedResponse?.data?.let {
                it.hentPerson?.navn?.firstOrNull()?.format()
            } ?: throw FunctionalPdlError("Fant ikke navn i pdl response. ${parsedResponse?.hentErrors()}")

        return navn
    }

    private fun createHeaders(): HttpHeaders {
        val headers = createHeader()

        headers[TEMA] = TEMA_SYK
        headers[BEHANDLINGSNUMMER_KEY] = BEHANDLINGSNUMMER_VALUE
        return headers
    }

    private fun createHeader(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return headers
    }

    private fun requestToJson(graphQLRequest: GraphQLRequest): String {
        return try {
            ObjectMapper().writeValueAsString(graphQLRequest)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    class FunctionalPdlError(message: String) : RuntimeException(message)
}

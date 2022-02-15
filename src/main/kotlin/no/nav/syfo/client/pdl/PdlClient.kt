package no.nav.syfo.client.pdl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.syfo.objectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.*

@Component
class PdlClient(
    @Value("\${PDL_URL}")
    private val pdlApiUrl: String,
    private val pdlRestTemplate: RestTemplate
) {

    private val TEMA = "Tema"
    private val TEMA_SYK = "SYK"
    private val IDENT = "ident"

    @Retryable(exclude = [FunctionalPdlError::class])
    fun hentIdenter(ident: String): HentIdenterResponseData {

        val graphQLRequest = GraphQLRequest(
            query = HENT_IDENTER_QUERY,
            variables = Collections.singletonMap(IDENT, ident)
        )

        val responseEntity = pdlRestTemplate.exchange(
            "$pdlApiUrl/graphql",
            HttpMethod.POST,
            HttpEntity(requestToJson(graphQLRequest), createHeaderWithTema()),
            String::class.java
        )

        if (responseEntity.statusCode != HttpStatus.OK) {
            throw RuntimeException("PDL svarer med status ${responseEntity.statusCode} - ${responseEntity.body}")
        }

        val parsedResponse: HentIdenterResponse? = responseEntity.body?.let { objectMapper.readValue(it) }

        parsedResponse?.data?.let {
            return it
        }
        throw FunctionalPdlError("Fant ikke person, ingen body eller data. ${parsedResponse.hentErrors()}")
    }

    @Retryable(exclude = [FunctionalPdlError::class])
    fun hentFormattertNavn(fnr: String): String {

        val graphQLRequest = GraphQLRequest(
            query = HENT_NAVN_QUERY,
            variables = Collections.singletonMap(IDENT, fnr)
        )

        val responseEntity = pdlRestTemplate.exchange(
            "$pdlApiUrl/graphql",
            HttpMethod.POST,
            HttpEntity(requestToJson(graphQLRequest), createHeaderWithTema()),
            String::class.java
        )

        if (responseEntity.statusCode != HttpStatus.OK) {
            throw RuntimeException("PDL svarer med status ${responseEntity.statusCode} - ${responseEntity.body}")
        }

        val parsedResponse: HentNavnResponse? = responseEntity.body?.let { objectMapper.readValue(it) }

        parsedResponse?.data?.let {
            return it.hentPerson?.navn?.firstOrNull()?.format()
                ?: throw FunctionalPdlError("Fant navn i pdl response. ${parsedResponse.hentErrors()}")
        }
        throw FunctionalPdlError("Fant ikke person, ingen body eller data. ${parsedResponse.hentErrors()}")
    }

    private fun createHeaderWithTema(): HttpHeaders {
        val headers = createHeader()
        headers[TEMA] = TEMA_SYK
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

    private fun HentIdenterResponse?.hentErrors(): String? {
        return this?.errors?.map { it.message }?.joinToString(" - ")
    }

    private fun HentNavnResponse?.hentErrors(): String? {
        return this?.errors?.map { it.message }?.joinToString(" - ")
    }

    data class GraphQLRequest(val query: String, val variables: Map<String, String>)

    class FunctionalPdlError(message: String) : RuntimeException(message)
}

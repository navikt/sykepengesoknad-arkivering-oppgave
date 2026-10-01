package no.nav.helse.flex.client.pdl

import no.nav.helse.flex.graphql.GraphQLRequest
import no.nav.helse.flex.graphql.GraphQLResponse
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.serialisertTilString
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import tools.jackson.module.kotlin.readValue
import java.util.*

private const val TEMA = "Tema"
private const val TEMA_SYK = "SYK"
private const val IDENT = "ident"
private const val BEHANDLINGSNUMMER_KEY = "Behandlingsnummer"
private const val BEHANDLINGSNUMMER_VALUE = "B139"

@Component
class PdlClient(
    @param:Value($$"${PDL_URL}")
    private val pdlApiUrl: String,
    private val pdlRestTemplate: RestTemplate,
) {
    @Retryable(excludes = [FunctionalPdlError::class])
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
            } ?: run {
                val responsFeil = parsedResponse?.hentErrors() ?: "Ingen oppgitte feil"
                throw FunctionalPdlError("Fant ikke person identer, ingen body eller data. PdlFeil: $responsFeil")
            }

        return identer
    }

    @Retryable(excludes = [FunctionalPdlError::class])
    fun hentFormattertNavn(fnr: String): String? {
        val graphQLRequest =
            GraphQLRequest(
                query = HENT_NAVN_QUERY,
                variables = Collections.singletonMap(IDENT, fnr),
            )

        val responseEntity =
            pdlRestTemplate.exchange<String>(
                "$pdlApiUrl/graphql",
                HttpMethod.POST,
                HttpEntity(requestToJson(graphQLRequest), createHeaders()),
            )

        if (responseEntity.statusCode != HttpStatus.OK) {
            throw RuntimeException("PDL svarer med status ${responseEntity.statusCode} - ${responseEntity.body}")
        }

        val parsedResponse =
            responseEntity.body?.let { objectMapper.readValue<GraphQLResponse<HentNavnResponseData>>(it) }
                ?: throw FunctionalPdlError("PDL respons er tom")

        val navn =
            parsedResponse.data.let {
                it.hentPerson
                    ?.navn
                    ?.firstOrNull()
                    ?.format()
            }

        if (navn == null && parsedResponse.harErrors()) {
            val responsFeil = parsedResponse.hentErrors() ?: "Ingen oppgitte feil"
            throw FunctionalPdlError("Fant ikke navn i pdl response. PdlFeil: $responsFeil")
        }

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

    private fun requestToJson(graphQLRequest: GraphQLRequest): String = graphQLRequest.serialisertTilString()

    class FunctionalPdlError(
        message: String,
    ) : RuntimeException(message)
}

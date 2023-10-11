package no.nav.helse.flex.mockdispatcher

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.client.pdl.*
import no.nav.helse.flex.graphql.GraphQLRequest
import no.nav.helse.flex.graphql.GraphQLResponse
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.serialisertTilString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest

object PdlMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        val graphReq = objectMapper.readValue<GraphQLRequest>(request.body.readUtf8())
        val ident = graphReq.variables["ident"] ?: return MockResponse().setStatus("400").setBody("Ingen ident variabel")

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        when (graphReq.query) {
            HENT_IDENTER_QUERY -> {
                return MockResponse().setBody(
                    GraphQLResponse(
                        data = HentIdenterResponseData(
                            hentIdenter = HentIdenter(
                                identer = listOf(ident)
                                    .map { PdlIdent(gruppe = FOLKEREGISTERIDENT, ident = it) }.toMutableList()
                                    .also { it.add(PdlIdent(gruppe = AKTORID, ident = ident + "00")) }
                            )
                        ),
                        errors = null
                    ).serialisertTilString()
                )
            }
            HENT_NAVN_QUERY -> {
                return MockResponse().setBody(
                    GraphQLResponse(
                        data = HentNavnResponseData(
                            hentPerson = HentNavn(
                                navn = listOf(Navn(fornavn = "Navn", mellomnavn = null, etternavn = "Navnesen"))
                            )
                        ),
                        errors = null
                    ).serialisertTilString()
                )
            }
        }

        return MockResponse().setResponseCode(404)
            .setBody("Har ikke implemetert pdl mock api for ${graphReq.query}")
    }
}

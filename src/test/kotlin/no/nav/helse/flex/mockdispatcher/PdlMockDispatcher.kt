package no.nav.helse.flex.mockdispatcher

import mockwebserver3.MockResponse
import mockwebserver3.QueueDispatcher
import mockwebserver3.RecordedRequest
import no.nav.helse.flex.bodyAsString
import no.nav.helse.flex.client.pdl.*
import no.nav.helse.flex.graphql.GraphQLRequest
import no.nav.helse.flex.graphql.GraphQLResponse
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.serialisertTilString
import tools.jackson.module.kotlin.readValue

object PdlMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        val graphReq = objectMapper.readValue<GraphQLRequest>(request.bodyAsString())
        val ident =
            graphReq.variables["ident"]
                ?: return MockResponse
                    .Builder()
                    .code(400)
                    .body("Ingen ident variabel")
                    .build()

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        when (graphReq.query) {
            HENT_IDENTER_QUERY -> {
                return MockResponse
                    .Builder()
                    .body(
                        GraphQLResponse(
                            data =
                                HentIdenterResponseData(
                                    hentIdenter =
                                        HentIdenter(
                                            identer =
                                                listOf(ident)
                                                    .map { PdlIdent(gruppe = FOLKEREGISTERIDENT, ident = it) }
                                                    .toMutableList()
                                                    .also { it.add(PdlIdent(gruppe = AKTORID, ident = ident + "00")) },
                                        ),
                                ),
                            errors = null,
                        ).serialisertTilString(),
                    ).build()
            }

            HENT_NAVN_QUERY -> {
                return MockResponse
                    .Builder()
                    .body(
                        GraphQLResponse(
                            data =
                                HentNavnResponseData(
                                    hentPerson =
                                        HentNavn(
                                            navn = listOf(Navn(fornavn = "Navn", mellomnavn = null, etternavn = "Navnesen")),
                                        ),
                                ),
                            errors = null,
                        ).serialisertTilString(),
                    ).build()
            }
        }

        return MockResponse
            .Builder()
            .code(404)
            .body("Har ikke implemetert pdl mock api for ${graphReq.query}")
            .build()
    }
}

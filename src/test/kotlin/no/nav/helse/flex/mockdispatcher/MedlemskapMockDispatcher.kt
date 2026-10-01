package no.nav.helse.flex.mockdispatcher

import mockwebserver3.MockResponse
import mockwebserver3.QueueDispatcher
import mockwebserver3.RecordedRequest
import no.nav.helse.flex.bodyAsString
import no.nav.helse.flex.jsonResponse
import no.nav.helse.flex.medlemskap.EndeligVurderingRequest
import no.nav.helse.flex.medlemskap.EndeligVurderingResponse
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.serialisertTilString
import tools.jackson.module.kotlin.readValue
import java.util.UUID

object MedlemskapMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (request.url.encodedPath != "/flexvurdering") {
            return MockResponse
                .Builder()
                .code(404)
                .body("Har ikke implemetert medlemskap mock api for ${request.url}")
                .build()
        }

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        val requestBody = objectMapper.readValue<EndeligVurderingRequest>(request.bodyAsString())

        return jsonResponse(
            EndeligVurderingResponse(
                sykepengesoknad_id = requestBody.sykepengesoknad_id,
                fnr = requestBody.fnr,
                fom = requestBody.fom,
                tom = requestBody.tom,
                vurdering_id = UUID.randomUUID().toString(),
                status = EndeligVurderingResponse.MedlemskapVurderingStatus.JA,
            ).serialisertTilString(),
        )
    }
}

package no.nav.helse.flex.mockdispatcher

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.medlemskap.EndeligVurderingRequest
import no.nav.helse.flex.medlemskap.EndeligVurderingResponse
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.serialisertTilString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest
import java.util.*

object MedlemskapMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (request.requestUrl?.encodedPath != "/flexvurdering") {
            return MockResponse().setResponseCode(404).setBody("Har ikke implemetert medlemskap mock api for ${request.requestUrl}")
        }

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        val requestBody = objectMapper.readValue<EndeligVurderingRequest>(request.body.readUtf8())

        return MockResponse()
            .setBody(
                EndeligVurderingResponse(
                    sykepengesoknad_id = requestBody.sykepengesoknad_id,
                    fnr = requestBody.fnr,
                    fom = requestBody.fom,
                    tom = requestBody.tom,
                    vurdering_id = UUID.randomUUID().toString(),
                    status = EndeligVurderingResponse.MedlemskapVurderingStatus.JA,
                ).serialisertTilString(),
            ).addHeader("Content-Type", "application/json")
    }
}

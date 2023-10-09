package no.nav.helse.flex.mockdispatcher

import no.nav.helse.flex.mockSykepengesoknadDTO
import no.nav.helse.flex.serialisertTilString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest

object SykepengesoknadMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        println(request.requestUrl)
        if (request.requestUrl?.encodedPath?.endsWith("/kafkaformat") != true) {
            return MockResponse().setResponseCode(404)
                .setBody("Har ikke implemetert sykepengesoknad mock api for ${request.requestUrl}")
        }

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        return MockResponse().setBody(
            mockSykepengesoknadDTO.serialisertTilString()
        ).addHeader("Content-Type", "application/json")
    }
}

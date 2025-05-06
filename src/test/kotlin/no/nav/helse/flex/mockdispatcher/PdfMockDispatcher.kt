package no.nav.helse.flex.mockdispatcher

import no.nav.helse.flex.serialisertTilString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest

object PdfMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (request.requestUrl?.encodedPath?.startsWith("/api/v1/genpdf/syfosoknader/") != true) {
            return MockResponse()
                .setResponseCode(404)
                .setBody("Har ikke implemetert pdf mock api for ${request.requestUrl}")
        }

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        return MockResponse().setBody(ByteArray(0).serialisertTilString())
    }
}

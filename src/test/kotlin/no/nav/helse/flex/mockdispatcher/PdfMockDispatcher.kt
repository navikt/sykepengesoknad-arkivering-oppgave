package no.nav.helse.flex.mockdispatcher

import mockwebserver3.MockResponse
import mockwebserver3.QueueDispatcher
import mockwebserver3.RecordedRequest
import no.nav.helse.flex.serialisertTilString

object PdfMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (!request.url.encodedPath.startsWith("/api/v1/genpdf/syfosoknader/")) {
            return MockResponse
                .Builder()
                .code(404)
                .body("Har ikke implemetert pdf mock api for ${request.url}")
                .build()
        }

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        return MockResponse
            .Builder()
            .body(ByteArray(0).serialisertTilString())
            .build()
    }
}

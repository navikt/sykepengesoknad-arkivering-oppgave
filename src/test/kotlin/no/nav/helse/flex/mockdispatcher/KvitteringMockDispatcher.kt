package no.nav.helse.flex.mockdispatcher

import mockwebserver3.MockResponse
import mockwebserver3.QueueDispatcher
import mockwebserver3.RecordedRequest
import okio.Buffer

object KvitteringMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (!request.url.encodedPath.startsWith("/maskin/kvittering")) {
            return MockResponse
                .Builder()
                .code(404)
                .body("Har ikke implemetert kvittering mock api for ${request.url}")
                .build()
        }

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        return MockResponse
            .Builder()
            .body(Buffer().readFrom("123".encodeToByteArray().inputStream()))
            .build()
    }
}

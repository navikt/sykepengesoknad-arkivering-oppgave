package no.nav.helse.flex.mockdispatcher

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer

object KvitteringMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (request.requestUrl?.encodedPath?.startsWith("/maskin/kvittering") != true) {
            return MockResponse()
                .setResponseCode(404)
                .setBody("Har ikke implemetert kvittering mock api for ${request.requestUrl}")
        }

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        return MockResponse().setBody(
            Buffer().readFrom("123".encodeToByteArray().inputStream()),
        )
    }
}

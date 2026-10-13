package no.nav.helse.flex.mockdispatcher

import mockwebserver3.MockResponse
import mockwebserver3.QueueDispatcher
import mockwebserver3.RecordedRequest
import no.nav.helse.flex.jsonResponse
import no.nav.helse.flex.mockSykepengesoknadDTO
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO

object SykepengesoknadMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (!request.url.encodedPath.endsWith("/kafkaformat")) {
            return MockResponse
                .Builder()
                .code(404)
                .body("Har ikke implemetert sykepengesoknad mock api for ${request.url}")
                .build()
        }

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        return jsonResponse(mockSykepengesoknadDTO.serialisertTilString())
    }

    fun enque(soknad: SykepengesoknadDTO) {
        enqueue(jsonResponse(soknad.serialisertTilString()))
    }
}

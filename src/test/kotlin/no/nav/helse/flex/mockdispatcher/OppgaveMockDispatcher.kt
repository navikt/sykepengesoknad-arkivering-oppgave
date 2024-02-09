package no.nav.helse.flex.mockdispatcher

import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.OpprettOppgaveResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest

object OppgaveMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (request.requestUrl?.encodedPath?.startsWith("/api/v1/oppgaver") != true) {
            return MockResponse().setResponseCode(404)
                .setBody("Har ikke implemetert oppgave mock api for ${request.requestUrl}")
        }

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        return MockResponse().setBody(
            OpprettOppgaveResponse(123, "4488", "SYK", "SOK").serialisertTilString(),
        ).addHeader("Content-Type", "application/json")
    }
}

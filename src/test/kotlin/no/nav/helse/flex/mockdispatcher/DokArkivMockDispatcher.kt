package no.nav.helse.flex.mockdispatcher

import no.nav.helse.flex.domain.DokumentInfo
import no.nav.helse.flex.domain.JournalpostResponse
import no.nav.helse.flex.serialisertTilString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest

object DokArkivMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (request.requestUrl?.encodedPath?.startsWith("/rest/journalpostapi/v1/journalpost") != true) {
            return MockResponse().setResponseCode(404)
                .setBody("Har ikke implemetert dok arkiv mock api for ${request.requestUrl}")
        }

        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }

        return MockResponse().setBody(
            JournalpostResponse(
                dokumenter = listOf(DokumentInfo()),
                journalpostId = "journalpostId",
                journalpostferdigstilt = true
            ).serialisertTilString()
        ).addHeader("Content-Type", "application/json")
    }
}

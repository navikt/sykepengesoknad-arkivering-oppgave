package no.nav.helse.flex.mockdispatcher

import no.nav.helse.flex.domain.DokumentInfo
import no.nav.helse.flex.domain.JournalpostResponse
import no.nav.helse.flex.domain.LogiskVedleggResponse
import no.nav.helse.flex.serialisertTilString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest

object DokArkivMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }
        if (request.requestUrl?.encodedPath?.startsWith("/rest/journalpostapi/v1/journalpost") == true) {
//            return MockResponse().setResponseCode(404)
//                .setBody("Har ikke implemetert dok arkiv mock api for ${request.requestUrl}")

            return MockResponse().setBody(
                JournalpostResponse(
                    dokumenter = listOf(DokumentInfo(dokumentInfoId = "123456")),
                    journalpostId = "journalpostId",
                    journalpostferdigstilt = true
                ).serialisertTilString()
            ).addHeader("Content-Type", "application/json")
        }

        // rest/journalpostapi/v1/dokumentInfo/{dokumentInfoId}/logiskVedlegg/
        if (request.requestUrl?.encodedPath?.endsWith("/logiskVedlegg") == true) {
//            return MockResponse().setResponseCode(404)
//                .setBody("Har ikke implemetert dok arkiv mock api for ${request.requestUrl}")
            return MockResponse().setBody(
                LogiskVedleggResponse(
                    logiskVedleggId = "323971844"
                ).serialisertTilString()
            ).addHeader("Content-Type", "application/json")
        }

        return MockResponse().setResponseCode(404).setBody("Har ikke implemetert dok arkiv mock api for ${request.requestUrl}")
    }
}

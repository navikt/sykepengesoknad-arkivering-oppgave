package no.nav.helse.flex.mockdispatcher

import mockwebserver3.MockResponse
import mockwebserver3.QueueDispatcher
import mockwebserver3.RecordedRequest
import no.nav.helse.flex.domain.DokumentInfo
import no.nav.helse.flex.domain.JournalpostResponse
import no.nav.helse.flex.domain.LogiskVedleggResponse
import no.nav.helse.flex.jsonResponse
import no.nav.helse.flex.serialisertTilString

object DokArkivMockDispatcher : QueueDispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (responseQueue.peek() != null) {
            return responseQueue.take()
        }
        if (request.url.encodedPath.startsWith("/rest/journalpostapi/v1/journalpost")) {
            return jsonResponse(
                JournalpostResponse(
                    dokumenter = listOf(DokumentInfo(dokumentInfoId = "123456")),
                    journalpostId = "journalpostId",
                    journalpostferdigstilt = true,
                ).serialisertTilString(),
            )
        }

        if (request.url.encodedPath.endsWith("/logiskVedlegg")) {
            return jsonResponse(
                LogiskVedleggResponse(
                    logiskVedleggId = "323971844",
                ).serialisertTilString(),
            )
        }

        return MockResponse
            .Builder()
            .code(404)
            .body("Har ikke implemetert dok arkiv mock api for ${request.url}")
            .build()
    }
}

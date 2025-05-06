package no.nav.helse.flex.mockdispatcher

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.HentOppgaveResponse
import no.nav.helse.flex.service.OppdaterOppgaveReqeust
import no.nav.helse.flex.service.OpprettOppgaveResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest
import org.springframework.http.MediaType

object OppgaveMockDispatcher : QueueDispatcher() {
    private val opprettOppgaveRequest = mutableListOf<RecordedRequest>()
    private val slettEttersendingRequests = mutableListOf<RecordedRequest>()
    private val patchOppgaveRequest = mutableListOf<RecordedRequest>()

    val log = logger()

    override fun dispatch(request: RecordedRequest): MockResponse {
        fun withContentTypeApplicationJson(createMockResponse: () -> MockResponse): MockResponse =
            createMockResponse().addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)

        if (responseQueue.peek() != null) {
            return withContentTypeApplicationJson { responseQueue.take() }
        }

        return if (request.requestLine == "POST /api/v1/oppgaver HTTP/1.1") {
            opprettOppgaveRequest.add(request)

            withContentTypeApplicationJson {
                MockResponse().setResponseCode(200).setBody(
                    OpprettOppgaveResponse(123, "4488", "SYK", "SOK").serialisertTilString(),
                )
            }
        } else if (request.requestLine.startsWith("GET /api/v1/oppgaver/")) {
            slettEttersendingRequests.add(request)

            withContentTypeApplicationJson {
                MockResponse().setResponseCode(200).setBody(
                    HentOppgaveResponse("OPPRETTET").serialisertTilString(),
                )
            }
        } else if (request.requestLine.startsWith("PATCH /api/v1/oppgaver/")) {
            patchOppgaveRequest.add(request)

            withContentTypeApplicationJson {
                MockResponse().setResponseCode(200).setBody(
                    HentOppgaveResponse("OPPRETTET").serialisertTilString(),
                )
            }
        } else {
            log.error("Ukjent api: " + request.requestLine)
            MockResponse().setResponseCode(404)
        }
    }

    fun getOppdaterOppgaveRequest(): List<RecordedRequest> = patchOppgaveRequest.toList()

    fun getLastOppdaterOppgaveReqeust(): OppdaterOppgaveReqeust = objectMapper.readValue(patchOppgaveRequest.last().body.readUtf8())

    fun reset() {
        opprettOppgaveRequest.clear()
        slettEttersendingRequests.clear()
        patchOppgaveRequest.clear()
    }
}

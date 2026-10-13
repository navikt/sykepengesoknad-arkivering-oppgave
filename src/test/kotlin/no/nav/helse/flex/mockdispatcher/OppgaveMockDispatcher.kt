package no.nav.helse.flex.mockdispatcher

import mockwebserver3.MockResponse
import mockwebserver3.QueueDispatcher
import mockwebserver3.RecordedRequest
import no.nav.helse.flex.bodyAsString
import no.nav.helse.flex.jsonResponse
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.HentOppgaveResponse
import no.nav.helse.flex.service.OppdaterOppgaveReqeust
import no.nav.helse.flex.service.OpprettOppgaveResponse
import org.springframework.http.MediaType
import tools.jackson.module.kotlin.readValue

object OppgaveMockDispatcher : QueueDispatcher() {
    private val opprettOppgaveRequest = mutableListOf<RecordedRequest>()
    private val slettEttersendingRequests = mutableListOf<RecordedRequest>()
    private val patchOppgaveRequest = mutableListOf<RecordedRequest>()

    val log = logger()

    override fun dispatch(request: RecordedRequest): MockResponse {
        if (responseQueue.peek() != null) {
            return responseQueue
                .take()
                .newBuilder()
                .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build()
        }

        return if (request.requestLine == "POST /api/v1/oppgaver HTTP/1.1") {
            opprettOppgaveRequest.add(request)

            jsonResponse(OpprettOppgaveResponse(123, "4488", "SYK", "SOK").serialisertTilString())
        } else if (request.requestLine.startsWith("GET /api/v1/oppgaver/")) {
            slettEttersendingRequests.add(request)

            jsonResponse(HentOppgaveResponse("OPPRETTET").serialisertTilString())
        } else if (request.requestLine.startsWith("PATCH /api/v1/oppgaver/")) {
            patchOppgaveRequest.add(request)

            jsonResponse(HentOppgaveResponse("OPPRETTET").serialisertTilString())
        } else {
            log.error("Ukjent api: " + request.requestLine)
            MockResponse(code = 404)
        }
    }

    fun getOppdaterOppgaveRequest(): List<RecordedRequest> = patchOppgaveRequest.toList()

    fun getLastOppdaterOppgaveReqeust(): OppdaterOppgaveReqeust = objectMapper.readValue(patchOppgaveRequest.last().bodyAsString())

    fun reset() {
        opprettOppgaveRequest.clear()
        slettEttersendingRequests.clear()
        patchOppgaveRequest.clear()
    }
}

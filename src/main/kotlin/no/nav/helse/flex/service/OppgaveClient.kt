package no.nav.helse.flex.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.helse.flex.util.callId
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class OppgaveClient(
    @Value("\${OPPGAVE_URL}")
    private val url: String,
    private val oppgaveRestTemplate: RestTemplate,
) {
    fun opprettOppgave(request: OppgaveRequest): OpprettOppgaveResponse {
        return try {
            val uriString = UriComponentsBuilder.fromHttpUrl("$url/api/v1/oppgaver")

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers["X-Correlation-ID"] = callId()

            val result =
                oppgaveRestTemplate
                    .exchange(
                        uriString.toUriString(),
                        HttpMethod.POST,
                        HttpEntity<Any>(request, headers),
                        OpprettOppgaveResponse::class.java,
                    )

            if (!result.statusCode.is2xxSuccessful) {
                throw RuntimeException("Oppretting av oppgave feiler med HTTP-${result.statusCode}")
            }

            result.body
                ?: throw RuntimeException("Oppgave-respons mangler ved oppretting av oppgave for journalpostId ${request.journalpostId}")
        } catch (e: HttpClientErrorException) {
            throw RuntimeException("Feil ved oppretting av oppgave for journalpostId ${request.journalpostId}", e)
        }
    }

    fun hentOppgave(oppgaveId: String): HentOppgaveResponse {
        TODO("Not yet implemented")
    }

    fun oppdaterOppgave(oppgaveId: String, oppdaterOppgaveReqeust: OppdaterOppgaveReqeust) {
        TODO("Not yet implemented")
    }
}

data class OppgaveRequest(
    var tildeltEnhetsnr: String? = null,
    val opprettetAvEnhetsnr: String,
    val aktoerId: String,
    val journalpostId: String,
    val beskrivelse: String,
    val tema: String,
    var behandlingstype: String? = null,
    var behandlingstema: String? = null,
    val oppgavetype: String,
    val aktivDato: String?,
    val fristFerdigstillelse: String?,
    val prioritet: String,
)

data class OppdaterOppgaveReqeust(
    var behandlingstype: String? = null,
    var behandlingstema: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpprettOppgaveResponse(
    val id: Int,
    val tildeltEnhetsnr: String,
    val tema: String,
    val oppgavetype: String,
    val behandlingstema: String? = null,
    val behandlingstype: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class HentOppgaveResponse(
    val status: String
)

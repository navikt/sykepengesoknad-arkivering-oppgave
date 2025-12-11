package no.nav.helse.flex.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.helse.flex.config.EnvironmentToggles
import no.nav.helse.flex.oppgave.log
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
    @param:Value("\${OPPGAVE_URL}")
    private val url: String,
    private val oppgaveRestTemplate: RestTemplate,
    private val environmentToggles: EnvironmentToggles,
) {
    fun opprettOppgave(request: OppgaveRequest): OpprettOppgaveResponse {
        return try {
            val uriString = UriComponentsBuilder.fromUriString("$url/api/v1/oppgaver")

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
            if (environmentToggles.isDevGcp() && e.message?.contains("Identen finnes ikke i PDL") == true) {
                log.info(
                    "Identen finnes ikke i PDL i dev. Returnerer konstruert oppgave med id -1 for journalpost " + request.journalpostId,
                )
                return OpprettOppgaveResponse(
                    id = -1,
                    tildeltEnhetsnr = "-1",
                    tema = "SYK",
                    oppgavetype = "-1",
                )
            }
            throw RuntimeException("Feil ved oppretting av oppgave for journalpostId ${request.journalpostId}", e)
        }
    }

    fun hentOppgave(oppgaveId: String): HentOppgaveResponse =
        try {
            val uriString = UriComponentsBuilder.fromUriString("$url/api/v1/oppgaver/$oppgaveId")

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers["X-Correlation-ID"] = callId()

            val result =
                oppgaveRestTemplate
                    .exchange(
                        uriString.toUriString(),
                        HttpMethod.GET,
                        HttpEntity<Any>(null, headers),
                        HentOppgaveResponse::class.java,
                    )

            if (!result.statusCode.is2xxSuccessful) {
                throw RuntimeException("Henting av oppgave feiler med HTTP-${result.statusCode}")
            }

            result.body
                ?: throw RuntimeException("Oppgave-respons mangler ved henting av oppgave for oppgaveid $oppgaveId")
        } catch (e: HttpClientErrorException) {
            throw RuntimeException("HttpClientErrorException ved henting av oppgave for oppgaveid $oppgaveId", e)
        }

    fun oppdaterOppgave(
        oppgaveId: String,
        oppdaterOppgaveReqeust: OppdaterOppgaveReqeust,
    ): HentOppgaveResponse =
        try {
            val uriString = UriComponentsBuilder.fromUriString("$url/api/v1/oppgaver/$oppgaveId")

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers["X-Correlation-ID"] = callId()

            val result =
                oppgaveRestTemplate
                    .exchange(
                        uriString.toUriString(),
                        HttpMethod.PATCH,
                        HttpEntity<Any>(oppdaterOppgaveReqeust, headers),
                        HentOppgaveResponse::class.java,
                    )

            if (!result.statusCode.is2xxSuccessful) {
                throw RuntimeException("Oppdatering av oppgave feiler med HTTP-${result.statusCode}")
            }

            result.body
                ?: throw RuntimeException("Oppgave-respons mangler ved oppdatering av oppgave for oppgaveid $oppgaveId")
        } catch (e: HttpClientErrorException) {
            throw RuntimeException("HttpClientErrorException ved oppdatering av oppgave for oppgaveid $oppgaveId", e)
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
    val status: String,
)

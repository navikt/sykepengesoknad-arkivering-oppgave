package no.nav.syfo.consumer.oppgave

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.syfo.config.CALL_ID
import no.nav.syfo.consumer.token.TokenConsumer
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.log
import no.nav.syfo.service.lagBeskrivelse
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter

@Component
class OppgaveConsumer(
    private val tokenConsumer: TokenConsumer,
    @Value("\${srvsyfogsak.username}") private val username: String,
    @Value("\${oppgave.oppgaver.url}") private val url: String,
    private val restTemplate: RestTemplate
) {
    val log = log()
    val oppgaveDato: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val uriString = UriComponentsBuilder.fromHttpUrl(url).toUriString()

    fun opprettOppgave(oppgaveRequest: OppgaveRequest): OppgaveResponse {
        val request = oppdaterRequestBodyMedDatoer(oppgaveRequest)
        return try {
            val result = restTemplate.exchange(
                uriString,
                HttpMethod.POST,
                HttpEntity(request, lagRequestHeaders()),
                OppgaveResponse::class.java
            )

            if (!result.statusCode.is2xxSuccessful) {
                throw RuntimeException("Oppretting av oppgave for aktør ${request.aktoerId} feiler med HTTP-${result.statusCode}")
            }

            result.body
                ?: throw RuntimeException("Oppgave-respons mangler ved oppretting av oppgave for ${request.aktoerId}")
        } catch (e: HttpClientErrorException) {
            throw RuntimeException("Feil ved oppretting av oppgave for aktør ${request.aktoerId}", e)
        }
    }

    fun lagRequestHeaders(): HttpHeaders = HttpHeaders().also { headers ->
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Authorization"] = "Bearer ${tokenConsumer.token.access_token}"
        headers["Nav-Call-Id"] = MDC.get(CALL_ID)
        headers["X-Correlation-ID"] = MDC.get(CALL_ID)
        headers["Nav-Consumer-Id"] = username
    }

    fun oppdaterRequestBodyMedDatoer(oppgaveRequest: OppgaveRequest) = oppgaveRequest.copy(
        aktivDato = now().format(oppgaveDato),
        fristFerdigstillelse = omTreUkedager(now()).format(oppgaveDato)
    )

    companion object {
        fun lagRequestBody(
            aktorId: String,
            behandlendeEnhet: String,
            saksId: String,
            journalpostId: String,
            soknad: Soknad
        ): OppgaveRequest =
            OppgaveRequest(
                tildeltEnhetsnr = behandlendeEnhet,
                opprettetAvEnhetsnr = "9999",
                aktoerId = aktorId,
                journalpostId = journalpostId,
                saksreferanse = saksId,
                beskrivelse = lagBeskrivelse(soknad),
                tema = "SYK",
                behandlingstema = if (soknad.soknadstype == Soknadstype.OPPHOLD_UTLAND) {
                    "ab0314"
                } else {
                    "ab0061"
                },
                oppgavetype = "SOK",
                aktivDato = null,
                fristFerdigstillelse = null,
                prioritet = "NORM"
            )
    }

    fun omTreUkedager(idag: LocalDate): LocalDate {
        return when (idag.dayOfWeek) {
            DayOfWeek.SUNDAY -> idag.plusDays(4)
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY -> idag.plusDays(3)
            else -> idag.plusDays(5)
        }
    }
}

data class OppgaveRequest(
    val tildeltEnhetsnr: String,
    val opprettetAvEnhetsnr: String,
    val aktoerId: String,
    val journalpostId: String,
    val saksreferanse: String,
    val beskrivelse: String,
    val tema: String,
    val behandlingstema: String,
    val oppgavetype: String,
    val aktivDato: String?,
    val fristFerdigstillelse: String?,
    val prioritet: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OppgaveResponse(
    val id: Int
)

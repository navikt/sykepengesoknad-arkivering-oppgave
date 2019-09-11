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
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter

@Component
class OppgaveConsumer(private val tokenConsumer: TokenConsumer,
                      @Value("\${srvsyfogsak.username}") private val username: String,
                      @Value("\${oppgave.oppgaver.url}") private val url: String,
                      private val restTemplate: RestTemplate) {

    val log = log()
    val oppgaveDato: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun opprettOppgave(aktorId: String, behandlendeEnhet: String, saksId: String, journalpostId: String, soknad: Soknad): String {
        val uriString = UriComponentsBuilder.fromHttpUrl(url).toUriString()

        try {
            val result = restTemplate.exchange(uriString,
                    HttpMethod.POST,
                    HttpEntity(lagRequestBody(aktorId, behandlendeEnhet, saksId, journalpostId, soknad), lagRequestHeaders()),
                    OppgaveResponse::class.java)

            if (result.statusCode != HttpStatus.CREATED) {
                val message = "Oppretting av oppgave for aktør $aktorId feiler med HTTP-" + result.statusCode
                log.error(message)
                throw RuntimeException(message)
            }

            return result
                    .let {
                        it.body
                                ?: throw RuntimeException("Oppgave-respons mangler ved oppretting av oppgave for $aktorId - skal ikke kunne skje!")
                    }
                    .id.toString()
        } catch (e: HttpClientErrorException) {
            log.error("Feil ved oppretting av oppgave for aktør $aktorId", e)
            throw RuntimeException(e)
        }
    }

    fun lagRequestHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer " + tokenConsumer.token.access_token)
        headers.set("Nav-Call-Id", MDC.get(CALL_ID))
        headers.set("X-Correlation-ID", MDC.get(CALL_ID))
        headers.set("Nav-Consumer-Id", username)
        return headers
    }

    fun lagRequestBody(aktorId: String, behandlendeEnhet: String, saksId: String, journalpostId: String, soknad: Soknad): OppgaveRequest =
            OppgaveRequest(
                    tildeltEnhetsnr = behandlendeEnhet,
                    opprettetAvEnhetsnr = "9999",
                    aktoerId = aktorId,
                    journalpostId = journalpostId,
                    saksreferanse = saksId,
                    beskrivelse = lagBeskrivelse(soknad),
                    tema = "SYK",
                    behandlingstema = if (soknad.soknadstype == Soknadstype.OPPHOLD_UTLAND) { "ab0314" } else { "ab0061" },
                    oppgavetype = "SOK",
                    aktivDato = now().format(oppgaveDato),
                    fristFerdigstillelse = omTreUkedager(now()).format(oppgaveDato),
                    prioritet = "NORM"
            )

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
        val aktivDato: String,
        val fristFerdigstillelse: String,
        val prioritet: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OppgaveResponse(
        val id: Int
)

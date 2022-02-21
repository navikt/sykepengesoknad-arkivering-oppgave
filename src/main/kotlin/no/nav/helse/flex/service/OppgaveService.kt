package no.nav.helse.flex.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Soknadstype
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter

@Service
class OppgaveService(
    @Value("\${OPPGAVE_URL}")
    private val url: String,
    private val oppgaveRestTemplate: RestTemplate
) {
    fun opprettOppgave(request: OppgaveRequest): OppgaveResponse {

        return try {
            val uriString = UriComponentsBuilder.fromHttpUrl(url)

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers["X-Correlation-ID"] = callId()

            val result = oppgaveRestTemplate
                .exchange(
                    uriString.toUriString(),
                    HttpMethod.POST,
                    HttpEntity<Any>(request, headers),
                    OppgaveResponse::class.java
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

    companion object {
        val oppgaveDato: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        fun lagRequestBody(
            aktorId: String,
            journalpostId: String,
            soknad: Soknad,
            harRedusertVenteperiode: Boolean = false,
            speilRelatert: Boolean = false,
        ): OppgaveRequest =
            OppgaveRequest(
                opprettetAvEnhetsnr = "9999",
                aktoerId = aktorId,
                journalpostId = journalpostId,
                beskrivelse = lagBeskrivelse(soknad),
                tema = "SYK",
                oppgavetype = "SOK",
                aktivDato = now().format(oppgaveDato),
                fristFerdigstillelse = omTreUkedager(now()).format(oppgaveDato),
                prioritet = "NORM"
            ).apply {
                if (harRedusertVenteperiode) {
                    this.behandlingstype = "ae0247"
                } else if (speilRelatert) {
                    this.behandlingstema = "ab0455"
                } else {
                    this.behandlingstema = when (soknad.soknadstype) {
                        Soknadstype.OPPHOLD_UTLAND -> "ab0314"
                        Soknadstype.BEHANDLINGSDAGER -> "ab0351"
                        Soknadstype.ARBEIDSLEDIG -> "ab0426"
                        Soknadstype.REISETILSKUDD, Soknadstype.GRADERT_REISETILSKUDD -> "ab0237"
                        else -> "ab0061"
                    }
                }
            }

        fun omTreUkedager(idag: LocalDate) = when (idag.dayOfWeek) {
            DayOfWeek.SUNDAY -> idag.plusDays(4)
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY -> idag.plusDays(3)
            else -> idag.plusDays(5)
        }
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
    val prioritet: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OppgaveResponse(
    val id: Int
)

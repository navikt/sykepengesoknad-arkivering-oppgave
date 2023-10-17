package no.nav.helse.flex.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.oppgave.lagBeskrivelse
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

private const val forkortetVentetid = "ae0247"
private const val overgangssakFraSpeil = "ab0455"
private const val utland = "ae0106"
private const val medlemskap = "ab0269"
private const val sykepengerUnderUtenlandsopphold = "ab0314"
private const val enkeltståendeBehandlingsdager = "ab0351"
private const val sykepengerForArbeidsledig = "ab0426"
private const val reisetilskudd = "ab0237"
private const val sykepenger = "ab0061"

@Service
class OppgaveClient(
    @Value("\${OPPGAVE_URL}")
    private val url: String,
    private val oppgaveRestTemplate: RestTemplate
) {
    fun opprettOppgave(request: OppgaveRequest): OppgaveResponse {
        return try {
            val uriString = UriComponentsBuilder.fromHttpUrl("$url/api/v1/oppgaver")

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
            medlemskapVurdering: String? = null
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
                    this.behandlingstype = forkortetVentetid
                } else if (speilRelatert) {
                    this.behandlingstema = overgangssakFraSpeil
                } else if (soknad.utenlandskSykmelding == true) {
                    this.behandlingstype = utland
                } else if (medlemskapVurdering in listOf("NEI", "UAVKLART")) {
                    this.behandlingstema = medlemskap
                } else {
                    this.behandlingstema = when (soknad.soknadstype) {
                        Soknadstype.OPPHOLD_UTLAND -> sykepengerUnderUtenlandsopphold
                        Soknadstype.BEHANDLINGSDAGER -> enkeltståendeBehandlingsdager
                        Soknadstype.ARBEIDSLEDIG -> sykepengerForArbeidsledig
                        Soknadstype.REISETILSKUDD, Soknadstype.GRADERT_REISETILSKUDD -> reisetilskudd
                        else -> sykepenger
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
    val id: Int,
    val tildeltEnhetsnr: String,
    val tema: String,
    val oppgavetype: String,
    val behandlingstema: String? = null,
    val behandlingstype: String? = null
)

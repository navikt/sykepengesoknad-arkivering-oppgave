package no.nav.helse.flex.client

import no.nav.helse.flex.domain.JournalpostRequest
import no.nav.helse.flex.domain.JournalpostResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class DokArkivClient(
    @Value("\${DOKARKIV_URL}")
    private val dokarkivUrl: String,
    private val dokArkivRestTemplate: RestTemplate
) {

    @Retryable(backoff = Backoff(delay = 5000))
    fun opprettJournalpost(pdfRequest: JournalpostRequest, id: String): JournalpostResponse {
        val url = "$dokarkivUrl/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Nav-Callid"] = id

        val entity = HttpEntity(pdfRequest, headers)

        val result = dokArkivRestTemplate.exchange(url, HttpMethod.POST, entity, JournalpostResponse::class.java)

        if (!result.statusCode.is2xxSuccessful) {
            throw RuntimeException("dokarkiv feiler med HTTP-${result.statusCode} for søknad med id: $id")
        }

        return result.body
            ?: throw RuntimeException("dokarkiv returnerer ikke data for søknad med id: $id")
    }
}

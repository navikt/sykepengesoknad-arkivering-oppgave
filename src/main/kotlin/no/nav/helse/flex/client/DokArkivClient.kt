package no.nav.helse.flex.client

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.domain.JournalpostRequest
import no.nav.helse.flex.domain.JournalpostResponse
import no.nav.helse.flex.domain.LogiskVedleggRequest
import no.nav.helse.flex.domain.LogiskVedleggResponse
import no.nav.helse.flex.objectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Component
class DokArkivClient(
    @Value("\${DOKARKIV_URL}")
    private val dokarkivUrl: String,
    private val dokArkivRestTemplate: RestTemplate
) {

    @Retryable(backoff = Backoff(delay = 5000))
    fun opprettJournalpost(pdfRequest: JournalpostRequest, id: String): JournalpostResponse {
        try {
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
        } catch (e: HttpClientErrorException.Conflict) {
            return objectMapper.readValue(e.responseBodyAsString)
        }
    }

    @Retryable(backoff = Backoff(delay = 5000))
    fun opprettLogiskVedlegg(logiskVedleggRequest: LogiskVedleggRequest, dokumentId: String): LogiskVedleggResponse{ // hvordan kan jeg unngå any her? hva er egentlig responsformatet? sendes det noe json? hvordan tester man?
        try {
            val url = "$dokarkivUrl/rest/journalpostapi/v1/dokumentInfo/$dokumentId/logiskVedlegg/"

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers["Nav-Callid"] = dokumentId

            val entity = HttpEntity(logiskVedleggRequest, headers)

            // val result = dokArkivRestTemplate.exchange(url, HttpMethod.POST, entity, JournalpostResponse::class.java)
            val result = dokArkivRestTemplate.exchange(url, HttpMethod.POST, entity, LogiskVedleggResponse::class.java)

            if (!result.statusCode.is2xxSuccessful) {
                throw RuntimeException("dokarkiv feiler med HTTP-${result.statusCode} for søknad med id: $dokumentId")
            }

            return result.body
                ?: throw RuntimeException("dokarkiv returnerer ikke data for søknad med id: $dokumentId")
        } catch (e: HttpClientErrorException.Conflict) {
            return objectMapper.readValue(e.responseBodyAsString)
        }
    }
}

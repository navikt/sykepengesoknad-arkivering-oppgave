package no.nav.helse.flex.client

import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.PDFTemplate
import no.nav.helse.flex.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class PDFClient(
    @Value("\${PDFGEN_URL}")
    private val pdfgenUrl: String,
    private val pdfGenRestTemplate: RestTemplate,
) {
    val log = logger()

    @Retryable(backoff = Backoff(delay = 5000))
    fun getPDF(
        soknad: Soknad,
        template: PDFTemplate,
    ): ByteArray {
        val url = "$pdfgenUrl/api/v1/genpdf/syfosoknader/" + template.endpoint

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(soknad, headers)

        val result = pdfGenRestTemplate.exchange(url, HttpMethod.POST, entity, ByteArray::class.java)

        if (result.statusCode != OK) {
            throw RuntimeException(
                "getPDF feiler med HTTP-" + result.statusCode + " for søknad om utenlandsopphold med id: " + soknad.soknadsId,
            )
        }

        return result.body!!
    }
}

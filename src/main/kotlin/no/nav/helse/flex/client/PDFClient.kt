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
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Component
class PDFClient(
    @param:Value("\${PDFGEN_URL}")
    private val pdfgenUrl: String,
    private val pdfGenRestTemplate: RestTemplate,
) {
    val log = logger()

    @Retryable(delay = 5000L)
    fun getPDF(
        soknad: Soknad,
        template: PDFTemplate,
    ): ByteArray {
        val url = "$pdfgenUrl/api/v1/genpdf/syfosoknader/" + template.endpoint

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(soknad, headers)

        val result = pdfGenRestTemplate.exchange<ByteArray>(url, HttpMethod.POST, entity)

        if (result.statusCode != OK) {
            throw RuntimeException(
                "getPDF feiler med HTTP-" + result.statusCode + " for søknad om utenlandsopphold med id: " + soknad.soknadsId,
            )
        }

        return result.body!!
    }
}

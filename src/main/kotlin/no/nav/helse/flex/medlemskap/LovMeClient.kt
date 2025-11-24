package no.nav.helse.flex.medlemskap

import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.lang.Exception
import java.time.LocalDate

@Component
class LovMeClient(
    private val medlemskapVurderingRestTemplate: RestTemplate,
    @Value("\${MEDLEMSKAP_VURDERING_URL}")
    private val url: String,
) {
    private val log = logger()

    fun hentEndeligMedlemskapVurdering(sykepengesoknad: Sykepengesoknad): EndeligVurderingResponse? {
        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.set("Nav-Call-Id", sykepengesoknad.id)

            val uri =
                UriComponentsBuilder
                    .fromUriString(url)
                    .pathSegment("flexvurdering")
                    .build()
                    .toUri()

            val request =
                EndeligVurderingRequest(
                    sykepengesoknad.id,
                    sykepengesoknad.fnr,
                    sykepengesoknad.fom!!,
                    sykepengesoknad.tom!!,
                )

            val result =
                medlemskapVurderingRestTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    HttpEntity(request, headers),
                    EndeligVurderingResponse::class.java,
                )

            return if (result.body != null) {
                result.body
            } else {
                log.error("Mottok svar fra LovMe, men uten endelig medlemskapsvurdering for sykepengesoknadId: ${sykepengesoknad.id}.")
                null
            }
        } catch (_: Exception) {
            log.error("Fant ikke endelig medlemskap vurdering for sykepengesoknadId ${sykepengesoknad.id}.")
            return null
        }
    }
}

data class EndeligVurderingRequest(
    val sykepengesoknad_id: String,
    val fnr: String,
    val fom: LocalDate,
    val tom: LocalDate,
)

data class EndeligVurderingResponse(
    val sykepengesoknad_id: String,
    val vurdering_id: String,
    val fnr: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val status: MedlemskapVurderingStatus,
) {
    enum class MedlemskapVurderingStatus {
        JA,
        NEI,
        UAVKLART,
    }
}

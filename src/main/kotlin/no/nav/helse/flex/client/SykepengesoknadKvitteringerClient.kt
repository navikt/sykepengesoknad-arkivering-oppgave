package no.nav.helse.flex.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class SykepengesoknadKvitteringerClient(
    @Value("\${SYKEPENGESOKNAD_KVITTERINGER_URL}")
    private val sykepengesoknadKvitteringerUrl: String,
    private val sykepengesoknadKvitteringerRestTemplate: RestTemplate
) {

    @Retryable(backoff = Backoff(delay = 5000))
    fun hentVedlegg(vedleggId: String): ByteArray {
        val url = "$sykepengesoknadKvitteringerUrl/maskin/kvittering/$vedleggId"

        val result = sykepengesoknadKvitteringerRestTemplate.exchange(url, HttpMethod.GET, null, ByteArray::class.java)

        if (!result.statusCode.is2xxSuccessful) {
            throw RuntimeException("Kall til sykepengesoknad-kvitteringer feilet med HTTP status kode: ${result.statusCode} for vedlegg med id: $vedleggId")
        }

        return result.body
            ?: throw RuntimeException("sykepengesoknad-kvitteringer returnerer ikke data for vedlegg med id: $vedleggId")
    }
}

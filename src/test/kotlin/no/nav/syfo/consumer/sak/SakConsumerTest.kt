package no.nav.syfo.consumer.sak

import no.nav.syfo.consumer.token.Token
import no.nav.syfo.consumer.token.TokenConsumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SakConsumerTest {
    @Mock
    lateinit var tokenConsumer: TokenConsumer

    @Mock
    lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var sakConsumer: SakConsumer

    @BeforeEach
    fun setup() {
        sakConsumer = SakConsumer(
            tokenConsumer = tokenConsumer,
            username = "username",
            url = "https://sak.nav.no",
            restTemplate = restTemplate
        )
        BDDMockito.given(tokenConsumer.token).willReturn(Token("token", "Bearer", 3600))
    }

/*    @Test
    fun opprettSakOppretterSakOgReturnererSakId() {
        val response = SakResponse(
            1234, "SYK", "FS22", "aktorId", null, null,
            "srvsyfogsak", "2019-03-19T09:16:18.824+01:00"
        )

        BDDMockito.given(
            restTemplate.exchange(
                BDDMockito.anyString(),
                BDDMockito.any(HttpMethod::class.java),
                BDDMockito.any(HttpEntity::class.java),
                BDDMockito.eq(SakResponse::class.java)
            )
        ).willReturn(ResponseEntity(response, HttpStatus.CREATED))

        val sakId = sakConsumer.opprettSak("aktorId")

        assertThat(sakId).isEqualTo("1234")
    }*/

    @Test
    fun opprettSakGirFeilmeldingHvisSakErNede() {

        assertThrows(RuntimeException::class.java) {
            BDDMockito.given(
                restTemplate.exchange(
                    BDDMockito.anyString(),
                    BDDMockito.any(HttpMethod::class.java),
                    BDDMockito.any(HttpEntity::class.java),
                    BDDMockito.eq(SakResponse::class.java)
                )
            ).willReturn(ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE))

            sakConsumer.opprettSak("aktorId")
        }
    }

    @Test
    fun lagRequestHeadersHarMedPaakrevdCorrelationId() {
        val headers = sakConsumer.lagRequestHeaders()

        assertThat(headers["X-Correlation-ID"]).isNotEmpty
    }

    @Test
    fun lagRequestBodyLagerRequestMedRiktigeFelter() {
        val body = sakConsumer.lagRequestBody("aktorId")

        assertThat(body.tema).isEqualTo("SYK")
        assertThat(body.applikasjon).isEqualTo("FS22")
        assertThat(body.aktoerId).isEqualTo("aktorId")
    }
}

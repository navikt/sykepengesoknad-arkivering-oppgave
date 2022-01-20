package no.nav.syfo.consumer.token

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.web.client.RestTemplate

@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
@EnableMockOAuth2Server
class TokenConsumerTest : AbstractContainerBaseTest() {
    @Mock
    lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var tokenConsumer: TokenConsumer

    private val token = Token("token", "Bearer", 3600)

    @BeforeEach
    fun setup() {
        tokenConsumer = TokenConsumer(
            restTemplate,
            url = "https://url.no"
        )
    }

    @Test
    fun tokenLeggesICache() {
        mockTokenRespone(token)
        val hentingNr1 = tokenConsumer.hentToken()
        assertThat(hentingNr1.access_token).isEqualTo("token")

        mockTokenRespone(token.copy(access_token = "token2"))
        val hentingNr2 = tokenConsumer.hentToken()
        assertThat(hentingNr2).isEqualTo(hentingNr1)
    }

    @Test
    fun tokenFornyes() {
        mockTokenRespone(token.copy(expires_in = 0))
        val hentingNr1 = tokenConsumer.hentToken()
        assertThat(hentingNr1.access_token).isEqualTo("token")

        mockTokenRespone(token.copy(access_token = "token2"))
        val hentingNr2 = tokenConsumer.hentToken()
        assertThat(hentingNr2).isNotEqualTo(hentingNr1)
        assertThat(hentingNr2.access_token).isEqualTo("token2")
    }

    private fun mockTokenRespone(token: Token) {
        given(
            restTemplate.exchange(
                BDDMockito.anyString(),
                BDDMockito.any(HttpMethod::class.java),
                BDDMockito.any(HttpEntity::class.java),
                BDDMockito.eq(Token::class.java)
            )
        ).willReturn(ResponseEntity(token, HttpStatus.OK))
    }
}

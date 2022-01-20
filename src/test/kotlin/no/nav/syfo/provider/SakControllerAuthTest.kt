package no.nav.syfo.provider

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.TestApplication
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import token

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = [TestApplication::class]
)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EnableMockOAuth2Server
class SakControllerAuthTest : AbstractContainerBaseTest() {

    @Autowired
    private lateinit var server: MockOAuth2Server

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun fungerMedGyldigToken() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/aktorId/sisteSak/")
                .header("Authorization", "Bearer ${server.token()}")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun fungerIkkeMedGyldigTokenOgKnektSignatur() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/aktorId/sisteSak/")
                .header("Authorization", """Bearer ${server.token()}sdsdf""")
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun feilerOmAppIdIkkeErNarmesteLeder() {
        val jwt = server.token(appId = "syfoslemming")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/aktorId/sisteSak/")
                .header("Authorization", "Bearer $jwt")
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun feilerOmAppIdMangler() {
        val jwt = server.token(appId = null)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/aktorId/sisteSak/")
                .header("Authorization", "Bearer $jwt")
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun feilerOmJwtMangler() {

        mockMvc.perform(MockMvcRequestBuilders.get("/aktorId/sisteSak/"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun feilerOmRequestHarFeilAudience() {
        val jwt = server.token(audience = "noe-annet")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/aktorId/sisteSak/")
                .header("Authorization", "Bearer $jwt")
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun feilerOmViHarFeilIssuer() {
        val jwt = server.token(issuerId = "blabla")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/aktorId/sisteSak/")
                .header("Authorization", "Bearer $jwt")
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }
}

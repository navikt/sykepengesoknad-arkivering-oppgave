package no.nav.syfo.filter

import buildClaimSet
import no.nav.security.oidc.test.support.JwtTokenGenerator
import no.nav.syfo.AZUREAD
import no.nav.syfo.TestApplication
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import javax.inject.Inject


@RunWith(SpringRunner::class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = [TestApplication::class])
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AzureADTokenFilterTest {

    @Inject
    private lateinit var mockMvc: MockMvc

    private val jwt = JwtTokenGenerator.createSignedJWT(buildClaimSet(subject = "syfoinntektsmelding", issuer = AZUREAD, appId = "syfoinntektsmelding_clientid", audience = "syfogsak_clientid")).serialize()

    @Test
    fun fungerMedGyldigToken() {
        mockMvc.perform(MockMvcRequestBuilders.get("/aktorId/sisteSak/")
                .header("Authorization", "Bearer $jwt"))
                .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun feilerOmAppIdIkkeErNarmesteLeder() {
        val jwt = JwtTokenGenerator.createSignedJWT(buildClaimSet(subject = "syfoinntektsmelding", issuer = AZUREAD, appId = "annen_clientid", audience = "syfogsak_clientid")).serialize()

        mockMvc.perform(MockMvcRequestBuilders.get("/aktorId/sisteSak/")
                .header("Authorization", "Bearer $jwt"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    @Test
    fun feilerOmRequestHarFeilAudience() {
        val jwt = JwtTokenGenerator.createSignedJWT(buildClaimSet(subject = "syfoinntektsmelding", issuer = AZUREAD, appId = "syfoinntektsmelding_clientid", audience = "annen audience")).serialize()

        mockMvc.perform(MockMvcRequestBuilders.get("/aktorId/sisteSak/")
                .header("Authorization", "Bearer $jwt"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    @Test
    fun feilerOmViHarFeilIssuer() {
        val jwt = JwtTokenGenerator.createSignedJWT(buildClaimSet(subject = "syfoinntektsmelding", issuer = "feil issuer", appId = "syfoinntektsmelding_clientid", audience = "syfogsak_clientid")).serialize()

        mockMvc.perform(MockMvcRequestBuilders.get("/aktorId/sisteSak/")
                .header("Authorization", "Bearer $jwt"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }
}

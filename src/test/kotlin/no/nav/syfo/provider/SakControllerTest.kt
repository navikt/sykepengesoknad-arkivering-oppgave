package no.nav.syfo.provider

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.TestApplication
import no.nav.syfo.consumer.repository.TidligereInnsending
import no.nav.syfo.consumer.repository.insertBehandletSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import token
import java.time.LocalDate

@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureMockMvc
@DirtiesContext
@EnableMockOAuth2Server
class SakControllerTest : AbstractContainerBaseTest() {

    @Autowired
    private lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate
    @Autowired
    private lateinit var sakController: SakController

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule())

    @Autowired
    private lateinit var server: MockOAuth2Server

    @AfterEach
    fun cleanup() {
        namedParameterJdbcTemplate.update("DELETE FROM INNSENDING", EmptySqlParameterSource())
    }

    @Test
    fun finnSisteSakHenterSisteSak() {
        namedParameterJdbcTemplate.insertBehandletSoknad(
            soknadFom = LocalDate.of(2019, 6, 1),
            soknadTom = LocalDate.of(2019, 6, 7),
            saksId = "sak1"
        )

        namedParameterJdbcTemplate.insertBehandletSoknad(
            soknadFom = LocalDate.of(2019, 6, 8),
            soknadTom = LocalDate.of(2019, 6, 17),
            saksId = "sak2"
        )

        namedParameterJdbcTemplate.insertBehandletSoknad(
            soknadFom = LocalDate.of(2019, 5, 1),
            soknadTom = LocalDate.of(2019, 5, 7),
            saksId = "sak3"
        )

        val result = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/aktor/sisteSak")
                    .header("Authorization", "Bearer ${server.token()}")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk).andReturn()

        val response =
            objectMapper.readValue(result.response.contentAsString, SakController.SisteSakRespons::class.java)
        assertThat(response.sisteSak).isEqualTo("sak2")
    }

    @Test
    fun finnerSisteSakSomOverlapperMedGrenseverdier() {
        namedParameterJdbcTemplate.insertBehandletSoknad(
            soknadFom = LocalDate.of(2019, 6, 1),
            soknadTom = LocalDate.of(2019, 6, 7),
            saksId = "sak1"
        )

        namedParameterJdbcTemplate.insertBehandletSoknad(
            soknadFom = LocalDate.of(2019, 6, 8),
            soknadTom = LocalDate.of(2019, 6, 17),
            saksId = "sak2"
        )

        namedParameterJdbcTemplate.insertBehandletSoknad(
            soknadFom = LocalDate.of(2019, 6, 20),
            soknadTom = LocalDate.of(2019, 6, 25),
            saksId = "sak3"
        )

        namedParameterJdbcTemplate.insertBehandletSoknad(
            soknadFom = LocalDate.of(2019, 5, 1),
            soknadTom = LocalDate.of(2019, 5, 7),
            saksId = "sak4"
        )

        val result = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/aktor/sisteSak?fom=2019-06-02&tom=2019-06-08")
                    .header("Authorization", "Bearer ${server.token()}")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk).andReturn()

        val response =
            objectMapper.readValue(result.response.contentAsString, SakController.SisteSakRespons::class.java)
        assertThat(response.sisteSak).isEqualTo("sak2")
    }

    @Test
    fun fomLikTomErGyldig() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/aktor/sisteSak?fom=2019-06-06&tom=2019-06-06")
                    .header("Authorization", "Bearer ${server.token()}")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk).andReturn()
    }

    @Test
    fun tomForFomErUgyldig() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/aktor/sisteSak?fom=2019-06-08&tom=2019-06-02")
                    .header("Authorization", "Bearer ${server.token()}")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().is4xxClientError).andReturn()
    }

    @Test
    fun finnerIkkeSak() {

        val contentAsString = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/aktor/sisteSak?fom=2019-06-02&tom=2019-06-08")
                    .header("Authorization", "Bearer ${server.token()}")
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk).andReturn().response.contentAsString

        assertThat(contentAsString).isEqualTo("{\"sisteSak\":null}")
    }

    val tidligereInnsending = TidligereInnsending(
        aktorId = "aktor",
        saksId = "saksId",
        behandlet = LocalDate.of(2019, 6, 10),
        soknadFom = LocalDate.of(2019, 6, 1),
        soknadTom = LocalDate.of(2019, 6, 10)
    )

    @Test
    fun soknadFomGrenseverdierOverlapper() {
        assertThat(
            overlapperMedSoknad(
                innsending = tidligereInnsending,
                fom = LocalDate.of(2019, 6, 1),
                tom = LocalDate.of(2019, 6, 15)
            )
        ).isTrue()
        assertThat(
            overlapperMedSoknad(
                innsending = tidligereInnsending,
                fom = LocalDate.of(2019, 6, 10),
                tom = LocalDate.of(2019, 6, 15)
            )
        ).isTrue()
    }

    @Test
    fun soknadTomGrenseverdierOverlapper() {
        assertThat(
            overlapperMedSoknad(
                innsending = tidligereInnsending,
                fom = LocalDate.of(2019, 5, 1),
                tom = LocalDate.of(2019, 6, 1)
            )
        ).isTrue()
        assertThat(
            overlapperMedSoknad(
                innsending = tidligereInnsending,
                fom = LocalDate.of(2019, 5, 1),
                tom = LocalDate.of(2019, 6, 10)
            )
        ).isTrue()
    }

    @Test
    fun soknadFomEllerTomInnenforGrenseOverlapper() {
        assertThat(
            overlapperMedSoknad(
                innsending = tidligereInnsending,
                fom = LocalDate.of(2019, 6, 2),
                tom = LocalDate.of(2019, 6, 15)
            )
        ).isTrue()
        assertThat(
            overlapperMedSoknad(
                innsending = tidligereInnsending,
                fom = LocalDate.of(2019, 6, 2),
                tom = LocalDate.of(2019, 6, 9)
            )
        ).isTrue()
        assertThat(
            overlapperMedSoknad(
                innsending = tidligereInnsending,
                fom = LocalDate.of(2019, 5, 2),
                tom = LocalDate.of(2019, 6, 9)
            )
        ).isTrue()
    }

    @Test
    fun soknaderForEllerEtterInntektsmeldingOverlapperIkke() {
        assertThat(
            overlapperMedSoknad(
                innsending = tidligereInnsending,
                fom = LocalDate.of(2019, 5, 2),
                tom = LocalDate.of(2019, 5, 31)
            )
        ).isFalse()
        assertThat(
            overlapperMedSoknad(
                innsending = tidligereInnsending,
                fom = LocalDate.of(2019, 6, 11),
                tom = LocalDate.of(2019, 6, 20)
            )
        ).isFalse()
    }
}

package no.nav.helse.flex.kafka

import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Kilde
import no.nav.helse.flex.domain.dto.KjentInntektskilde
import no.nav.helse.flex.kafka.mapper.toSykepengesoknad
import no.nav.helse.flex.mockSykepengesoknadDTO
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.sykepengesoknad.kafka.KildeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.KjenteInntektskilderDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GhostInntekterTest {
    @Test
    fun `ghost inntekter fra Kafka blir med i PDF payload`() {
        val ghostInntekter =
            setOf(
                KjenteInntektskilderDTO("Bensinstasjonen AS", KildeDTO.INNTEKTSKOMPONENTEN, "999333666"),
            )
        val kafkaSoknad = mockSykepengesoknadDTO.copy(flereInntektskilderGhost = ghostInntekter)
        val sykepengesoknad = kafkaSoknad.toSykepengesoknad("aktorId")
        assertThat(sykepengesoknad.flereInntektskilderGhost)
            .containsExactly(KjentInntektskilde("Bensinstasjonen AS", Kilde.INNTEKTSKOMPONENTEN, "999333666"))
        val pdfSoknad = Soknad.lagSoknad(sykepengesoknad, kafkaSoknad.fnr, "Testperson")
        val payload = objectMapper.readTree(objectMapper.writeValueAsString(pdfSoknad))

        assertThat(payload.get("flereInntektskilderGhost"))
            .isEqualTo(objectMapper.readTree(objectMapper.writeValueAsString(ghostInntekter)))
    }
}

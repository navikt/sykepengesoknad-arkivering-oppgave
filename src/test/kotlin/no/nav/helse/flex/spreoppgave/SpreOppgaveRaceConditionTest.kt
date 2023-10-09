package no.nav.helse.flex.spreoppgave

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.any
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.kafka.consumer.SPREOPPGAVER_TOPIC
import no.nav.helse.flex.kafka.consumer.SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.mockSykepengesoknadDTO
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.spreoppgave.HandterOppgaveInterceptor.Companion.raceConditionTimeout
import no.nav.helse.flex.spreoppgave.HandterOppgaveInterceptor.Companion.raceConditionUUID
import no.nav.helse.flex.util.tilOsloZone
import okhttp3.mockwebserver.MockResponse
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@DirtiesContext
class SpreOppgaveRaceConditionTest : FellesTestoppsett() {
    @Autowired
    lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @SpyBean
    private lateinit var handterOppagave: HandterOppgaveInterceptor

    private val søknad = mockSykepengesoknadDTO.copy(
        id = raceConditionUUID.toString(),
        sendtNav = LocalDateTime.now()
    )

    @BeforeEach
    fun setup() {
        sykepengesoknadMockWebserver.enqueue(MockResponse().setBody(søknad.serialisertTilString()).addHeader("Content-Type", "application/json"))
    }

    @AfterEach
    fun `slett spre oppgaver`() {
        jdbcTemplate.update("DELETE FROM oppgavestyring")
    }

    @Test
    fun `Håndtering av DuplicateKeyException når spre oppgave opprettes i fra søknad og bømlo samtidig`() {
        val timeoutFraBømlo = LocalDateTime.now().plusMinutes(29).truncatedTo(ChronoUnit.SECONDS)

        aivenKafkaProducer.send(
            ProducerRecord(
                SPREOPPGAVER_TOPIC,
                raceConditionUUID.serialisertTilString(),
                OppgaveDTO(
                    dokumentType = DokumentTypeDTO.Søknad,
                    oppdateringstype = OppdateringstypeDTO.Utsett,
                    dokumentId = raceConditionUUID,
                    timeout = timeoutFraBømlo
                ).serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10)).until {
            spreOppgaveRepository.findBySykepengesoknadId(
                raceConditionUUID.toString()
            )?.timeout == timeoutFraBømlo.tilOsloZone().toInstant()
        }

        // Skal kalles 2 ganger, første gang gir DuplicateKeyException, andre gang går ok
        verify(handterOppagave, times(2)).håndterOppgaveFraBømlo(any(), any())

        spreOppgaveRepository.findBySykepengesoknadId(
            raceConditionUUID.toString()
        )!!.timeout shouldBeEqualTo timeoutFraBømlo.tilOsloZone().toInstant()
    }

    @Test
    fun `Håndteres også når søknaden kaster DuplicateKeyException`() {
        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                søknad.id,
                søknad.serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10)).until {
            spreOppgaveRepository.findBySykepengesoknadId(
                raceConditionUUID.toString()
            )?.avstemt == true
        }

        // Skal kalles 2 ganger, første gang gir DuplicateKeyException, andre gang går ok
        verify(handterOppagave, times(2)).håndterOppgaveFraSøknad(any(), any())

        spreOppgaveRepository.findBySykepengesoknadId(
            raceConditionUUID.toString()
        )!!.timeout shouldBeEqualTo raceConditionTimeout
    }
}

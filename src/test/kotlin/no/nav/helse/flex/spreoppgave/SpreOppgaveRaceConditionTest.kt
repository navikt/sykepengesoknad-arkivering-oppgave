package no.nav.helse.flex.spreoppgave

import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.kafka.consumer.SPREOPPGAVER_TOPIC
import no.nav.helse.flex.kafka.consumer.SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.mockSykepengesoknadDTO
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.spreoppgave.HandterOppgaveInterceptor.Companion.kallTilHåndterOppgaveFraBømlo
import no.nav.helse.flex.spreoppgave.HandterOppgaveInterceptor.Companion.kallTilHåndterOppgaveFraSøknad
import no.nav.helse.flex.spreoppgave.HandterOppgaveInterceptor.Companion.raceConditionTimeout
import no.nav.helse.flex.spreoppgave.HandterOppgaveInterceptor.Companion.raceConditionUUID
import no.nav.helse.flex.util.tilOsloZone
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class SpreOppgaveRaceConditionTest : FellesTestoppsett() {
    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    private val søknad = mockSykepengesoknadDTO.copy(
        id = raceConditionUUID.toString(),
        sendtNav = LocalDateTime.now()
    )

    @AfterEach
    fun `slett spre oppgaver`() {
        jdbcTemplate.update("DELETE FROM oppgavestyring")
    }

    @Test
    fun `Håndtering av DuplicateKeyException når spre oppgave opprettes i fra søknad og bømlo samtidig`() {
        val timeoutFraBømlo = LocalDateTime.now().plusMinutes(29).truncatedTo(ChronoUnit.SECONDS)
        kallTilHåndterOppgaveFraBømlo = 0

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

        spreOppgaveRepository.findBySykepengesoknadId(
            raceConditionUUID.toString()
        )!!.timeout shouldBeEqualTo timeoutFraBømlo.tilOsloZone().toInstant()

        kallTilHåndterOppgaveFraBømlo shouldBeEqualTo 2
    }

    @Test
    fun `Håndteres også når søknaden kaster DuplicateKeyException`() {
        kallTilHåndterOppgaveFraSøknad = 0

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

        spreOppgaveRepository.findBySykepengesoknadId(
            raceConditionUUID.toString()
        )!!.timeout shouldBeEqualTo raceConditionTimeout

        kallTilHåndterOppgaveFraSøknad shouldBeEqualTo 2
    }
}

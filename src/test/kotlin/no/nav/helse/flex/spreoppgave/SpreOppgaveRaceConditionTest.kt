package no.nav.helse.flex.spreoppgave

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.any
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.kafka.consumer.SPREOPPGAVER_TOPIC
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.serialisertTilString
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import java.time.Duration
import java.time.LocalDateTime

class SpreOppgaveRaceConditionTest : FellesTestoppsett() {
    @Autowired
    lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @SpyBean
    private lateinit var handterOppagave: HandterOppave

    @Test
    fun `Håndtering av DuplicateKeyException når spre oppgave opprettes i fra søknad og bømlo samtidig`() {
        val timeoutFraBømlo = LocalDateTime.now().plusHours(1)

        aivenKafkaProducer.send(
            ProducerRecord(
                SPREOPPGAVER_TOPIC,
                OppgaveDTO(
                    dokumentType = DokumentTypeDTO.Søknad,
                    oppdateringstype = OppdateringstypeDTO.Utsett,
                    dokumentId = HandterOppgaveInterceptor.raceConditionUUID,
                    timeout = timeoutFraBømlo,
                ).serialisertTilString()
            )
        )

        Awaitility.await().atMost(Duration.ofSeconds(10)).until {
            spreOppgaveRepository.findBySykepengesoknadId(
                HandterOppgaveInterceptor.raceConditionUUID.toString()
            )?.avstemt == true
        }

        verify(handterOppagave, times(2)).håndterOppgaveFraBømlo(any(), any())
    }
}

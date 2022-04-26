package no.nav.helse.flex.oppgavefordeling

import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.mockSykepengesoknadDTO
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.util.tilOsloZone
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.util.UUID

@DirtiesContext
class AvstemMedSoknaderTest : FellesTestoppsett() {

    @Autowired
    lateinit var oppgavefordelingRepository: OppgavefordelingRepository

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @Test
    fun `Avstemning av oppgave`() {
        val id = UUID.randomUUID()
        val fnr = "12345"
        val soknad = mockSykepengesoknadDTO.copy(
            id = id.toString(),
            fnr = fnr
        )

        oppgavefordelingRepository.insert(
            sykepengesoknadId = soknad.id,
            status = OppgavefordelingStatus.LagOppgave,
        )

        oppgavefordelingRepository.findBySykepengesoknadId(soknad.id)?.avstemt shouldBeEqualTo false

        aivenKafkaProducer.send(
            ProducerRecord(
                SENDT_SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(2)).until {
            oppgavefordelingRepository.findBySykepengesoknadId(soknad.id)?.avstemt == true
        }

        val oppgave = oppgavefordelingRepository.findBySykepengesoknadId(soknad.id)!!
        oppgave.avstemt shouldBeEqualTo true
        oppgave.sendtNav shouldBeEqualTo soknad.sendtNav!!.tilOsloZone().toInstant()
    }
}

package no.nav.helse.flex.oppgavefordeling

import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.oppgavefordeling.FodselsnummerSoknader.EnkelSoknad
import no.nav.helse.flex.oppgavefordeling.FodselsnummerSoknader.Soknadstatus.SENDT
import no.nav.helse.flex.oppgavefordeling.FodselsnummerSoknader.Soknadstype.ARBEIDSTAKERE
import no.nav.helse.flex.serialisertTilString
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@DirtiesContext
class FodselsnummerSoknaderTest : FellesTestoppsett() {

    @Autowired
    lateinit var oppgavefordelingRepository: OppgavefordelingRepository

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    private fun mockSoknad(
        soknadId: UUID,
        fnr: String,
    ) = EnkelSoknad(
        id = soknadId.toString(),
        status = SENDT,
        sendtNav = LocalDateTime.now(),
        soknadstype = ARBEIDSTAKERE,
        fnr = fnr,
    )

    @Test
    fun `lagrer fnr med batchInsert`() {
        val uuid1 = UUID.randomUUID()
        val fnr1 = "12345678901"
        val soknad1 = mockSoknad(uuid1, fnr1)

        val uuid2 = UUID.randomUUID()
        val fnr2 = "09876543210"
        val soknad2 = mockSoknad(uuid2, fnr2)

        oppgavefordelingRepository.insert(
            sykepengesoknadId = soknad1.id,
            status = OppgavefordelingStatus.LagOppgave,
        )
        oppgavefordelingRepository.insert(
            sykepengesoknadId = soknad2.id,
            status = OppgavefordelingStatus.LagOppgave,
        )

        aivenKafkaProducer.send(
            ProducerRecord(SENDT_SYKEPENGESOKNAD_TOPIC, soknad1.id, soknad1.serialisertTilString())
        )
        aivenKafkaProducer.send(
            ProducerRecord(SENDT_SYKEPENGESOKNAD_TOPIC, soknad2.id, soknad2.serialisertTilString())
        )

        await().atMost(Duration.ofSeconds(2)).until {
            oppgavefordelingRepository.findBySykepengesoknadId(soknad1.id)?.fnr != null &&
                oppgavefordelingRepository.findBySykepengesoknadId(soknad2.id)?.fnr != null
        }

        oppgavefordelingRepository.findBySykepengesoknadId(soknad1.id)?.fnr shouldBeEqualTo fnr1
        oppgavefordelingRepository.findBySykepengesoknadId(soknad2.id)?.fnr shouldBeEqualTo fnr2
    }
}

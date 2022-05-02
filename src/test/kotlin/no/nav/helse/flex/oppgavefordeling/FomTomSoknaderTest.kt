package no.nav.helse.flex.oppgavefordeling

import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.oppgavefordeling.FomTomSoknader.EnkelSoknad
import no.nav.helse.flex.oppgavefordeling.FomTomSoknader.Soknadstatus.SENDT
import no.nav.helse.flex.oppgavefordeling.FomTomSoknader.Soknadstype.ARBEIDSTAKERE
import no.nav.helse.flex.serialisertTilString
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@DirtiesContext
class FomTomSoknaderTest : FellesTestoppsett() {

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
        fom = LocalDate.of(2022, 5, 1),
        tom = LocalDate.of(2022, 5, 2),
    )

    @Test
    fun `lagrer fnr med batchInsert`() {
        val uuid1 = UUID.randomUUID()
        val fnr1 = "12345678901"
        val soknad1 = mockSoknad(uuid1, fnr1)

        oppgavefordelingRepository.insert(
            sykepengesoknadId = soknad1.id,
            status = OppgavefordelingStatus.LagOppgave,
        )

        aivenKafkaProducer.send(
            ProducerRecord(SENDT_SYKEPENGESOKNAD_TOPIC, soknad1.id, soknad1.serialisertTilString())
        )

        await().atMost(Duration.ofSeconds(2)).until {
            oppgavefordelingRepository.findBySykepengesoknadId(soknad1.id)?.fom != null
        }

        oppgavefordelingRepository.findBySykepengesoknadId(soknad1.id)?.fom shouldBeEqualTo LocalDate.of(2022, 5, 1)
        oppgavefordelingRepository.findBySykepengesoknadId(soknad1.id)?.tom shouldBeEqualTo LocalDate.of(2022, 5, 2)
    }
}

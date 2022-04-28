package no.nav.helse.flex.oppgavefordeling

import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.oppgavefordeling.AvstemMedSoknader.EnkelSoknad
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.util.tilOsloZone
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@DirtiesContext
class SendtArbeidsgiverSoknaderTest : FellesTestoppsett() {

    @Autowired
    lateinit var oppgavefordelingRepository: OppgavefordelingRepository

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    private fun mockSoknad(soknadId: UUID) = EnkelSoknad(
        id = soknadId.toString(),
        status = AvstemMedSoknader.Soknadstatus.SENDT,
        sendtNav = LocalDateTime.now(),
        soknadstype = AvstemMedSoknader.Soknadstype.ARBEIDSTAKERE,
    )

    @Test
    fun `lagrer sendtTilArbeidsgiver med batchInsert`() {
        val uuid = UUID.randomUUID()
        val sentArbeidsgiver = LocalDateTime.now()

        val soknad = mockSoknad(uuid).copy(sendtArbeidsgiver = sentArbeidsgiver)

        oppgavefordelingRepository.insert(
            sykepengesoknadId = soknad.id,
            status = OppgavefordelingStatus.LagOppgave,
        )

        aivenKafkaProducer.send(
            ProducerRecord(
                SENDT_SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString()
            )
        )

        Awaitility.await().atMost(Duration.ofSeconds(2)).until {
            oppgavefordelingRepository.findBySykepengesoknadId(soknad.id)?.sendtArbeidsgiver != null
        }

        val oppgave = oppgavefordelingRepository.findBySykepengesoknadId(soknad.id)!!
        oppgave.sendtArbeidsgiver?.truncatedTo(ChronoUnit.MILLIS) shouldBeEqualTo soknad.sendtArbeidsgiver!!.tilOsloZone()
            .toInstant().truncatedTo(ChronoUnit.MILLIS)
    }
}

package no.nav.helse.flex.rebehandling

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import mockwebserver3.MockResponse
import no.nav.helse.flex.FellesTestOppsett
import no.nav.helse.flex.kafka.consumer.SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.mockSykepengesoknadDTO
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.serialisertTilString
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

class RebehandlingIntegrationTest : FellesTestOppsett() {
    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @Test
    fun `Behandling av søknad feiler og rebehandles`() {
        val fnr = "fnr"

        pdfMockWebserver.enqueue(MockResponse(code = 500))
        pdfMockWebserver.enqueue(MockResponse(code = 500))
        pdfMockWebserver.enqueue(MockResponse(code = 500))

        val id = UUID.randomUUID().toString()
        val soknad = mockSykepengesoknadDTO.copy(id = id, fnr = fnr)
        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                id,
                soknad.serialisertTilString(),
            ),
        )

        // Er satt opp med retry mot pdf, etter 3 feil legges søknaden til rebehandling
        pdfMockWebserver.takeRequest(30, TimeUnit.SECONDS)!!
        innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet shouldBe null
        pdfMockWebserver.takeRequest(30, TimeUnit.SECONDS)!!
        innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet shouldBe null
        pdfMockWebserver.takeRequest(30, TimeUnit.SECONDS)!!
        innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet shouldBe null

        // Venter til søknaden er rebehandlet
        pdfMockWebserver.takeRequest(30, TimeUnit.SECONDS)!!
        await().atMost(Duration.ofSeconds(30)).until {
            innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet != null
        }
        val innsending = innsendingRepository.findBySykepengesoknadId(soknad.id)!!
        innsending.behandlet shouldNotBe null
        innsending.sykepengesoknadId shouldBe soknad.id
        innsending.oppgaveId shouldBe null

        val spreOppgave = spreOppgaveRepository.findBySykepengesoknadId(soknad.id)
        spreOppgave!!.sykepengesoknadId shouldBe soknad.id
        spreOppgave.status shouldBe OppgaveStatus.VenterPaBomlo
    }
}

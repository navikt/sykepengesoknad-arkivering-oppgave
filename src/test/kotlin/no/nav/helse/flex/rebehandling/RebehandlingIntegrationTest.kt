package no.nav.helse.flex.rebehandling

import no.nav.helse.flex.FellesTestOppsett
import no.nav.helse.flex.kafka.consumer.SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.mockSykepengesoknadDTO
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.serialisertTilString
import okhttp3.mockwebserver.MockResponse
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
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

        pdfMockWebserver.enqueue(MockResponse().setResponseCode(500))
        pdfMockWebserver.enqueue(MockResponse().setResponseCode(500))
        pdfMockWebserver.enqueue(MockResponse().setResponseCode(500))

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
        innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet shouldBeEqualTo null
        pdfMockWebserver.takeRequest(30, TimeUnit.SECONDS)!!
        innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet shouldBeEqualTo null
        pdfMockWebserver.takeRequest(30, TimeUnit.SECONDS)!!
        innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet shouldBeEqualTo null

        // Venter til søknaden er rebehandlet
        pdfMockWebserver.takeRequest(30, TimeUnit.SECONDS)!!
        await().atMost(Duration.ofSeconds(30)).until {
            innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet != null
        }
        val innsending = innsendingRepository.findBySykepengesoknadId(soknad.id)!!
        innsending.behandlet shouldNotBe null
        innsending.sykepengesoknadId shouldBeEqualTo soknad.id
        innsending.oppgaveId shouldBeEqualTo null

        val spreOppgave = spreOppgaveRepository.findBySykepengesoknadId(soknad.id)
        spreOppgave!!.sykepengesoknadId shouldBeEqualTo soknad.id
        spreOppgave.status shouldBeEqualTo OppgaveStatus.Utsett
    }
}

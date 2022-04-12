package no.nav.helse.flex.rebehandling

import com.nhaarman.mockitokotlin2.*
import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.client.DokArkivClient
import no.nav.helse.flex.client.PDFClient
import no.nav.helse.flex.domain.DokumentInfo
import no.nav.helse.flex.domain.JournalpostResponse
import no.nav.helse.flex.kafka.consumer.SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.mockSykepengesoknadDTO
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.serialisertTilString
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.util.*

@DirtiesContext
class RebehandlingIntegrationTest : FellesTestoppsett() {

    @MockBean
    private lateinit var pdfClient: PDFClient

    @MockBean
    private lateinit var dokArkivClient: DokArkivClient

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @Autowired
    private lateinit var innsendingRepository: InnsendingRepository

    @Autowired
    private lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @Test
    fun `Behandling av søknad feiler og rebehandles`() {
        val fnr = "fnr"

        whenever(pdfClient.getPDF(any(), any())).thenThrow(RuntimeException("OOOPS")).thenReturn("pdf".toByteArray())

        whenever(dokArkivClient.opprettJournalpost(any(), any())).thenReturn(
            JournalpostResponse(
                dokumenter = listOf(
                    DokumentInfo()
                ),
                journalpostId = "1",
                journalpostferdigstilt = true,
            )
        )

        val id = UUID.randomUUID().toString()
        val soknad = mockSykepengesoknadDTO.copy(id = id, fnr = fnr)
        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                id,
                soknad.serialisertTilString()
            )
        )

        // Det skal ta ca 10 sekunder grunnet rebehandlinga
        await().between(Duration.ofSeconds(8), Duration.ofSeconds(20))
            .until {
                innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet != null
            }

        val innsending = innsendingRepository.findBySykepengesoknadId(soknad.id)!!
        innsending.behandlet shouldNotBe null
        innsending.sykepengesoknadId shouldBeEqualTo soknad.id
        innsending.oppgaveId shouldBeEqualTo null

        val spreOppgave = spreOppgaveRepository.findBySykepengesoknadId(soknad.id)
        spreOppgave!!.sykepengesoknadId shouldBeEqualTo soknad.id
        spreOppgave.status shouldBeEqualTo OppgaveStatus.Utsett

        verify(pdfClient, times(2)).getPDF(any(), any())
    }
}

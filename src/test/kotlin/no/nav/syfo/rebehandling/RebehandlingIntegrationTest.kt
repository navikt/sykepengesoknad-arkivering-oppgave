package no.nav.syfo.rebehandling

import com.nhaarman.mockitokotlin2.*
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.TestApplication
import no.nav.syfo.client.DokArkivClient
import no.nav.syfo.client.PDFClient
import no.nav.syfo.domain.DokumentInfo
import no.nav.syfo.domain.JournalpostResponse
import no.nav.syfo.kafka.consumer.SYKEPENGESOKNAD_TOPIC
import no.nav.syfo.mockSykepengesoknadDTO
import no.nav.syfo.repository.InnsendingDAO
import no.nav.syfo.repository.OppgaveStatus
import no.nav.syfo.repository.OppgavestyringDAO
import no.nav.syfo.serialisertTilString
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.util.*

@SpringBootTest(classes = [TestApplication::class])
@EnableMockOAuth2Server
@DirtiesContext
class RebehandlingIntegrationTest : AbstractContainerBaseTest() {

    @MockBean
    private lateinit var pdfClient: PDFClient

    @MockBean
    private lateinit var dokArkivClient: DokArkivClient

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @Autowired
    private lateinit var innsendingDAO: InnsendingDAO

    @Autowired
    private lateinit var oppgavestyringDAO: OppgavestyringDAO

    @Test
    fun `Behandling av søknad feiler og rebehandles`() {
        val aktorId = "298374918"
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
        await().between(Duration.ofSeconds(8), Duration.ofSeconds(12))
            .until {
                innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id)?.behandlet != null
            }

        val innsending = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id)
        innsending?.behandlet shouldNotBe null
        innsending!!.aktorId shouldBeEqualTo aktorId
        innsending.ressursId shouldBeEqualTo soknad.id
        innsending.oppgaveId shouldBeEqualTo null

        val spreOppgave = oppgavestyringDAO.hentSpreOppgave(soknad.id)
        spreOppgave!!.søknadsId shouldBeEqualTo soknad.id
        spreOppgave.status shouldBeEqualTo OppgaveStatus.Utsett

        verify(pdfClient, times(2)).getPDF(any(), any())
    }
}

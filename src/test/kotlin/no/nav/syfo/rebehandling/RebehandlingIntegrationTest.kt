package no.nav.syfo.rebehandling

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.OVERGANG
import no.nav.syfo.TestApplication
import no.nav.syfo.consumer.aktor.AktorConsumer
import no.nav.syfo.consumer.pdf.PDFConsumer
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.sak.SakConsumer
import no.nav.syfo.kafka.consumer.SYKEPENGESOKNAD_TOPIC
import no.nav.syfo.mockSykepengesoknadDTO
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
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(classes = [TestApplication::class])
@EnableMockOAuth2Server
@DirtiesContext
class RebehandlingIntegrationTest : AbstractContainerBaseTest() {

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @MockBean
    private lateinit var aktorConsumer: AktorConsumer

    @MockBean
    private lateinit var sakConsumer: SakConsumer

    @MockBean
    private lateinit var pdfConsumer: PDFConsumer

    @Autowired
    private lateinit var innsendingDAO: InnsendingDAO

    @Autowired
    private lateinit var oppgavestyringDAO: OppgavestyringDAO

    @Test
    fun `Behandling av søknad feiler og rebehandles`() {
        val aktorId = "aktor"
        val fnr = "fnr"
        val saksId = "saksId"

        val old = OVERGANG
        OVERGANG = LocalDateTime.now() // TODO: Fjern

        whenever(aktorConsumer.getAktorId(fnr)).thenReturn(aktorId)
        whenever(aktorConsumer.finnFnr(aktorId)).thenReturn(fnr)
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)
        whenever(pdfConsumer.getPDF(any(), any())).thenThrow(RuntimeException("OOOPS")).thenReturn("pdf".toByteArray())

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
        innsending.saksId shouldBeEqualTo saksId
        innsending.ressursId shouldBeEqualTo soknad.id
        innsending.oppgaveId shouldBeEqualTo null

        val spreOppgave = oppgavestyringDAO.hentSpreOppgave(soknad.id)
        spreOppgave!!.søknadsId shouldBeEqualTo soknad.id
        spreOppgave.status shouldBeEqualTo OppgaveStatus.Utsett

        verify(pdfConsumer, times(2)).getPDF(any(), any())

        OVERGANG = old
    }
}

package no.nav.syfo.rebehandling

import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.syfo.TestApplication
import no.nav.syfo.config.RebehandlingProducerMock
import no.nav.syfo.consumer.aktor.AktorConsumer
import no.nav.syfo.consumer.oppgave.OppgaveConsumer
import no.nav.syfo.consumer.oppgave.OppgaveRequest
import no.nav.syfo.consumer.oppgave.OppgaveResponse
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.sak.SakConsumer
import no.nav.syfo.controller.PDFRestController
import no.nav.syfo.kafka.consumer.RebehandlingListener
import no.nav.syfo.kafka.consumer.SoknadSendtListener
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SporsmalDTO
import no.nav.syfo.kafka.felles.SvarDTO
import no.nav.syfo.kafka.felles.SvartypeDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.skapConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@RunWith(SpringRunner::class)
@EmbeddedKafka
@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
class RebehandlingIntegrationTest {


    @Autowired
    private lateinit var rebehandlingProducerMock: RebehandlingProducerMock

    @Autowired
    private lateinit var soknadSendtListener: SoknadSendtListener

    @Autowired
    private lateinit var rebehandlingListener: RebehandlingListener

    @Mock
    private lateinit var acknowledgment: Acknowledgment

    @Mock
    private lateinit var acknowledgmentRebehandling: Acknowledgment

    @MockBean
    private lateinit var aktorConsumer: AktorConsumer

    @MockBean
    private lateinit var sakConsumer: SakConsumer

    @MockBean
    private lateinit var pdfRestController: PDFRestController

    @MockBean
    private lateinit var oppgaveConsumer: OppgaveConsumer

    @Autowired
    private lateinit var innsendingDAO: InnsendingDAO

    @After
    fun tearDown() {
        rebehandlingProducerMock.topicMeldinger.clear()
    }

    @Test
    fun `kan ikke hente aktor id legges på rebehandling`() {
        whenever(aktorConsumer.finnFnr(any())).thenThrow(RuntimeException("Gæli"))

        val soknad = SykepengesoknadDTO(
                aktorId = "aktor",
                id = "hei",
                opprettet = LocalDateTime.now(),
                type = SoknadstypeDTO.ARBEIDSTAKERE,
                sporsmal = emptyList(),
                status = SoknadsstatusDTO.SENDT,
                fodselsnummer = null
        )
        soknadSendtListener.listen(skapConsumerRecord(soknad.id!!, soknad), acknowledgment)
        verify(acknowledgment).acknowledge()


        assertThat(rebehandlingProducerMock.topicMeldinger).hasSize(1)

        rebehandlingListener.listen(rebehandlingProducerMock.hentSisteSomConsumerRecord(), acknowledgmentRebehandling)
        verify(acknowledgmentRebehandling).acknowledge()

        assertThat(rebehandlingProducerMock.topicMeldinger).hasSize(2)

        val innsendingIDatabase = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id!!)!!
        assertThat(innsendingIDatabase.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isNull()
        assertThat(innsendingIDatabase.behandlet).isNull()
        rebehandlingProducerMock.topicMeldinger.clear()
    }

    @Test
    fun `Vi rebehandler en søknad hvor vi først ikke fikk henta aktørid`() {
        val aktorId = "aktor"
        whenever(aktorConsumer.finnFnr(aktorId)).thenThrow(RuntimeException("Gæli")).thenReturn("fnr")
        val saksId = "saksId"
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)
        val oppgaveID = 1
        whenever(oppgaveConsumer.opprettOppgave(any())).thenReturn(OppgaveResponse(id = oppgaveID))

        val soknad = SykepengesoknadDTO(
                aktorId = aktorId,
                id = UUID.randomUUID().toString(),
                opprettet = LocalDateTime.now(),
                fom = LocalDate.of(2019, 5, 4),
                tom = LocalDate.of(2019, 5, 8),
                type = SoknadstypeDTO.ARBEIDSTAKERE,
                sporsmal = listOf(SporsmalDTO(
                        id = UUID.randomUUID().toString(),
                        tag = "TAGGEN",
                        sporsmalstekst = "Fungerer rebehandlinga?",
                        svartype = SvartypeDTO.JA_NEI,
                        svar = listOf(SvarDTO(verdi = "JA"))

                )),
                status = SoknadsstatusDTO.SENDT,
                sendtNav = LocalDateTime.now(),
                fodselsnummer = null
        )
        soknadSendtListener.listen(skapConsumerRecord(soknad.id!!, soknad), acknowledgment)
        verify(acknowledgment).acknowledge()


        assertThat(rebehandlingProducerMock.topicMeldinger).hasSize(1)
        val innsendingIDatabaseEtterFeiling = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id!!)!!
        assertThat(innsendingIDatabaseEtterFeiling.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabaseEtterFeiling.oppgaveId).isNull()
        assertThat(innsendingIDatabaseEtterFeiling.behandlet).isNull()

        //Rebehandle
        rebehandlingListener.listen(rebehandlingProducerMock.hentSisteSomConsumerRecord(), acknowledgmentRebehandling)
        verify(acknowledgmentRebehandling).acknowledge()

        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        verify(oppgaveConsumer, never()).opprettOppgave(captor.capture())

        val innsendingIDatabaseEtterRebehandling = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id!!)!!
        assertThat(innsendingIDatabaseEtterRebehandling.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabaseEtterRebehandling.oppgaveId).isNull()
        assertThat(innsendingIDatabaseEtterRebehandling.behandlet).isNotNull()

        assertThat(innsendingIDatabaseEtterRebehandling.innsendingsId).isEqualTo(innsendingIDatabaseEtterFeiling.innsendingsId)
    }

    @Test
    fun `Vi rebehandler en søknad der innsending ikke lagres`() {
        val aktorId = "aktor"
        whenever(aktorConsumer.finnFnr(aktorId)).thenReturn("fnr")
        val saksId = "saksId"
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)
        val oppgaveID = 1
        whenever(oppgaveConsumer.opprettOppgave(any())).thenReturn(OppgaveResponse(id = oppgaveID))
        val soknad = SykepengesoknadDTO(
                aktorId = aktorId,
                id = UUID.randomUUID().toString(),
                opprettet = LocalDateTime.now(),
                fom = LocalDate.of(2019, 5, 4),
                tom = LocalDate.of(2019, 5, 8),
                type = SoknadstypeDTO.ARBEIDSTAKERE,
                sporsmal = listOf(SporsmalDTO(
                        id = UUID.randomUUID().toString(),
                        tag = "TAGGEN",
                        sporsmalstekst = "Fungerer rebehandlinga?",
                        svartype = SvartypeDTO.JA_NEI,
                        svar = listOf(SvarDTO(verdi = "JA"))

                )),
                status = SoknadsstatusDTO.SENDT,
                sendtNav = LocalDateTime.now(),
                fodselsnummer = null
        ).toSykepengesoknad()

        //Rebehandle uten innsending i database
        rebehandlingProducerMock.leggPaRebehandlingTopic(soknad, LocalDateTime.now())
        rebehandlingListener.listen(rebehandlingProducerMock.hentSisteSomConsumerRecord(), acknowledgmentRebehandling)
        verify(acknowledgmentRebehandling).acknowledge()

        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        verify(oppgaveConsumer, never()).opprettOppgave(captor.capture())

        val innsendingIDatabaseEtterRebehandling = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id)!!
        assertThat(innsendingIDatabaseEtterRebehandling.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabaseEtterRebehandling.oppgaveId).isNull()
        assertThat(innsendingIDatabaseEtterRebehandling.behandlet).isNotNull()
    }
}

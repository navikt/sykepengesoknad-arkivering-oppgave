package no.nav.syfo.saksbehandling

import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.syfo.TestApplication
import no.nav.syfo.consumer.aktor.AktorConsumer
import no.nav.syfo.consumer.oppgave.OppgaveConsumer
import no.nav.syfo.consumer.oppgave.OppgaveRequest
import no.nav.syfo.consumer.oppgave.OppgaveResponse
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.sak.SakConsumer
import no.nav.syfo.controller.PDFRestController
import no.nav.syfo.kafka.consumer.SoknadSendtListener
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SporsmalDTO
import no.nav.syfo.kafka.felles.SvarDTO
import no.nav.syfo.kafka.felles.SvartypeDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.skapConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@RunWith(SpringRunner::class)
@EmbeddedKafka
@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
class SaksbehandlingIntegrationTest {


    @MockBean
    private lateinit var aktorConsumer: AktorConsumer

    @MockBean
    private lateinit var sakConsumer: SakConsumer

    @MockBean
    private lateinit var pdfRestController: PDFRestController

    @MockBean
    private lateinit var oppgaveConsumer: OppgaveConsumer

    @Mock
    private lateinit var acknowledgment: Acknowledgment

    @Inject
    private lateinit var soknadSendtListener: SoknadSendtListener

    @Inject
    private lateinit var innsendingDAO: InnsendingDAO


    @Test
    fun `test happycase`() {
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
                        sporsmalstekst = "Har systemet gode integrasjonstester?",
                        svartype = SvartypeDTO.JA_NEI,
                        svar = listOf(SvarDTO(verdi = "JA"))

                )),
                status = SoknadsstatusDTO.SENDT,
                sendtNav = LocalDateTime.now(),
                fodselsnummer = null
        )

        soknadSendtListener.listen(skapConsumerRecord(soknad.id!!, soknad), acknowledgment)

        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        verify(oppgaveConsumer).opprettOppgave(captor.capture())

        val oppgaveRequest = captor.firstValue
        assertThat(oppgaveRequest.aktoerId).isEqualTo(aktorId)
        assertThat(oppgaveRequest.journalpostId).isEqualTo("journalpostId")
        assertThat(oppgaveRequest.saksreferanse).isEqualTo(saksId)
        assertThat(oppgaveRequest.beskrivelse).isEqualTo(
                """
Søknad om sykepenger for perioden 04.05.2019 - 08.05.2019

Har systemet gode integrasjonstester?
Ja""".trimIndent())
        assertThat(oppgaveRequest.tema).isEqualTo("SYK")
        assertThat(oppgaveRequest.oppgavetype).isEqualTo("SOK")
        assertThat(oppgaveRequest.prioritet).isEqualTo("NORM")
        assertThat(oppgaveRequest.behandlingstema).isEqualTo("ab0061")


        val innsendingIDatabase = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id!!)!!
        assertThat(innsendingIDatabase.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isEqualTo(oppgaveID.toString())
        assertThat(innsendingIDatabase.behandlet).isNotNull()
    }

    @Test
    fun `Kafkamelding med redusertVenteperiode setter riktig behandlingstema`() {
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
            fom = LocalDate.of(2020, 5, 1),
            tom = LocalDate.of(2020, 5, 5),
            type = SoknadstypeDTO.SELVSTENDIGE_OG_FRILANSERE,
            sporsmal = listOf(SporsmalDTO(
                id = UUID.randomUUID().toString(),
                tag = "TAGGEN",
                sporsmalstekst = "Har systemet gode integrasjonstester?",
                svartype = SvartypeDTO.JA_NEI,
                svar = listOf(SvarDTO(verdi = "JA"))

            )),
            status = SoknadsstatusDTO.SENDT,
            sendtNav = LocalDateTime.now(),
            fodselsnummer = null,
            harRedusertVenteperiode = true
        )

        soknadSendtListener.listen(skapConsumerRecord(soknad.id!!, soknad), acknowledgment)

        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        verify(oppgaveConsumer).opprettOppgave(captor.capture())

        val oppgaveRequest = captor.firstValue
        assertThat(oppgaveRequest.aktoerId).isEqualTo(aktorId)
        assertThat(oppgaveRequest.journalpostId).isEqualTo("journalpostId")
        assertThat(oppgaveRequest.saksreferanse).isEqualTo(saksId)
        assertThat(oppgaveRequest.beskrivelse).isEqualTo(
            """
Søknad om sykepenger fra Selvstendig Næringsdrivende / Frilanser for perioden 01.05.2020 - 05.05.2020

Har systemet gode integrasjonstester?
Ja""".trimIndent())
        assertThat(oppgaveRequest.tema).isEqualTo("SYK")
        assertThat(oppgaveRequest.oppgavetype).isEqualTo("SOK")
        assertThat(oppgaveRequest.prioritet).isEqualTo("NORM")
        assertThat(oppgaveRequest.behandlingstema).isNull()
        assertThat(oppgaveRequest.behandlingstype).isEqualTo("ae0247")

        val innsendingIDatabase = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id!!)!!
        assertThat(innsendingIDatabase.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isEqualTo(oppgaveID.toString())
        assertThat(innsendingIDatabase.behandlet).isNotNull()
    }
}

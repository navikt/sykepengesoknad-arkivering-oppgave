package no.nav.syfo.saksbehandling

import com.nhaarman.mockitokotlin2.*
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.TestApplication
import no.nav.syfo.consumer.aktor.AktorConsumer
import no.nav.syfo.consumer.bucket.FlexBucketUploaderClient
import no.nav.syfo.consumer.oppgave.OppgaveConsumer
import no.nav.syfo.consumer.oppgave.OppgaveRequest
import no.nav.syfo.consumer.oppgave.OppgaveResponse
import no.nav.syfo.consumer.pdf.PDFConsumer
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.sak.SakConsumer
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.PDFTemplate
import no.nav.syfo.domain.dto.Svartype
import no.nav.syfo.kafka.consumer.SoknadSendtListener
import no.nav.syfo.kafka.felles.*
import no.nav.syfo.mock.BehandleJournalMock
import no.nav.syfo.mock.PersonMock
import no.nav.syfo.skapConsumerRecord
import no.nav.syfo.util.OBJECT_MAPPER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@EmbeddedKafka
@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
@EnableMockOAuth2Server
class SaksbehandlingIntegrationTest {

    @MockBean
    private lateinit var aktorConsumer: AktorConsumer

    @MockBean
    private lateinit var sakConsumer: SakConsumer

    @MockBean
    private lateinit var pdfConsumer: PDFConsumer

    @MockBean
    private lateinit var oppgaveConsumer: OppgaveConsumer

    @MockBean
    private lateinit var flexBucketUploaderClient: FlexBucketUploaderClient

    @Autowired
    private lateinit var behandleJournalV2: BehandleJournalMock

    @Autowired
    private lateinit var personMock: PersonMock

    @Mock
    private lateinit var acknowledgment: Acknowledgment

    @Autowired
    private lateinit var soknadSendtListener: SoknadSendtListener

    @Autowired
    private lateinit var innsendingDAO: InnsendingDAO

    @Test
    fun `test happycase`() {
        val aktorId = "aktor"
        whenever(aktorConsumer.finnFnr(aktorId)).thenReturn("fnr")
        val saksId = "saksId"
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)
        val oppgaveID = 1
        whenever(oppgaveConsumer.opprettOppgave(any())).thenReturn(OppgaveResponse(id = oppgaveID))

        val soknad = DeprecatedSykepengesoknadDTO(
            aktorId = aktorId,
            id = UUID.randomUUID().toString(),
            opprettet = LocalDateTime.now(),
            fom = LocalDate.of(2019, 5, 4),
            tom = LocalDate.of(2019, 5, 8),
            type = SoknadstypeDTO.ARBEIDSTAKERE,
            sporsmal = listOf(
                SporsmalDTO(
                    id = UUID.randomUUID().toString(),
                    tag = "TAGGEN",
                    sporsmalstekst = "Har systemet gode integrasjonstester?",
                    svartype = SvartypeDTO.JA_NEI,
                    svar = listOf(SvarDTO(verdi = "JA"))

                )
            ),
            status = SoknadsstatusDTO.SENDT,
            sendtNav = LocalDateTime.now(),
            fodselsnummer = null
        )

        soknadSendtListener.listen(skapConsumerRecord(soknad.id!!, soknad), acknowledgment)

        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        verify(oppgaveConsumer, never()).opprettOppgave(captor.capture())

        val innsendingIDatabase = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id!!)!!
        assertThat(innsendingIDatabase.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isNull()
        assertThat(innsendingIDatabase.behandlet).isNotNull()
        verify(pdfConsumer).getPDF(any(), eq(PDFTemplate.ARBEIDSTAKERE))

        val journalreq = behandleJournalV2.sisteJournalfoerInngaaendeHenvendelseRequest
        assertThat(journalreq!!.journalpost.dokumentinfoRelasjon.first().journalfoertDokument.dokumentType.value).isEqualTo("NAV 08-07.04 D")
    }

    @Test
    fun `Kafkamelding med redusertVenteperiode setter riktig behandlingstema`() {
        val aktorId = "aktor"
        whenever(aktorConsumer.finnFnr(aktorId)).thenReturn("fnr")
        val saksId = "saksId"
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)
        val oppgaveID = 1
        whenever(oppgaveConsumer.opprettOppgave(any())).thenReturn(OppgaveResponse(id = oppgaveID))

        val soknad = DeprecatedSykepengesoknadDTO(
            aktorId = aktorId,
            id = UUID.randomUUID().toString(),
            opprettet = LocalDateTime.now(),
            fom = LocalDate.of(2020, 5, 1),
            tom = LocalDate.of(2020, 5, 5),
            type = SoknadstypeDTO.SELVSTENDIGE_OG_FRILANSERE,
            sporsmal = listOf(
                SporsmalDTO(
                    id = UUID.randomUUID().toString(),
                    tag = "TAGGEN",
                    sporsmalstekst = "Har systemet gode integrasjonstester?",
                    svartype = SvartypeDTO.JA_NEI,
                    svar = listOf(SvarDTO(verdi = "JA"))

                )
            ),
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
Ja
            """.trimIndent()
        )
        assertThat(oppgaveRequest.tema).isEqualTo("SYK")
        assertThat(oppgaveRequest.oppgavetype).isEqualTo("SOK")
        assertThat(oppgaveRequest.prioritet).isEqualTo("NORM")
        assertThat(oppgaveRequest.behandlingstema).isNull()
        assertThat(oppgaveRequest.behandlingstype).isEqualTo("ae0247")

        val innsendingIDatabase = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id!!)!!
        assertThat(innsendingIDatabase.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isEqualTo(oppgaveID.toString())
        assertThat(innsendingIDatabase.behandlet).isNotNull()
        verify(pdfConsumer).getPDF(any(), eq(PDFTemplate.SELVSTENDIGNAERINGSDRIVENDE))
    }

    @Test
    fun `Reisetilskudd søknad behandles korrekt`() {
        val aktorId = "aktor"
        val fnr = "fnr"
        whenever(aktorConsumer.finnFnr(aktorId)).thenReturn(fnr)
        val saksId = "saksId"
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)

        whenever(flexBucketUploaderClient.hentVedlegg(any())).thenReturn("123".encodeToByteArray())
        val oppgaveID = 1
        whenever(oppgaveConsumer.opprettOppgave(any())).thenReturn(OppgaveResponse(id = oppgaveID))

        val soknad = OBJECT_MAPPER.readValue(
            TestApplication::class.java.getResource("/reisetilskuddAlleSvar.json"),
            DeprecatedSykepengesoknadDTO::class.java
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
Søknad om reisetilskudd for perioden 18.03.2021 - 22.03.2021

Søknaden har vedlagt 2 kvitteringer med en sum på 1 338,00 kr

Arbeidsgiver: Barnehagen
Organisasjonsnummer: 123454543

Periode 1:
18.03.2021 - 22.03.2021

Brukte du bil eller offentlig transport til og fra jobben?
Ja
    Hva slags type transport bruker du?
        Offentlig transport
            Hvor mye betaler du vanligvis i måneden for offentlig transport?
            20,00 kr

        Bil
            Hvor mange kilometer er kjøreturen mellom hjemmet ditt og jobben?
            42 km

Reiste du med egen bil, leiebil eller kollega til jobben mellom 18. - 22. mars 2021?
Ja
    Hvilke dager reiste du med bil?
    22.03.2021
    21.03.2021

    Hadde du utgifter til bompenger?
    Ja
        Hvor mye betalte du i bompenger mellom hjemmet ditt og jobben?
        30,00 kr

Legger arbeidsgiveren din ut for reisene?
Nei
            """.trimIndent()
        )
        assertThat(oppgaveRequest.tema).isEqualTo("SYK")
        assertThat(oppgaveRequest.oppgavetype).isEqualTo("SOK")
        assertThat(oppgaveRequest.prioritet).isEqualTo("NORM")
        assertThat(oppgaveRequest.behandlingstema).isEqualTo("ab0237")
        assertThat(oppgaveRequest.behandlingstype).isNull()
        assertThat(oppgaveRequest.tildeltEnhetsnr).isEqualTo("4488")

        val innsendingIDatabase = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id!!)!!
        assertThat(innsendingIDatabase.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isEqualTo(oppgaveID.toString())
        assertThat(innsendingIDatabase.behandlet).isNotNull()

        val pdfReqCaptor: KArgumentCaptor<Soknad> = argumentCaptor()
        verify(pdfConsumer).getPDF(pdfReqCaptor.capture(), eq(PDFTemplate.REISETILSKUDD))

        val pdfReq = pdfReqCaptor.firstValue
        assertThat(pdfReq.kvitteringSum).isEqualTo(133800)
        assertThat(pdfReq.kvitteringer).hasSize(2)
        assertThat(pdfReq.kvitteringer!![0].b64data).isEqualTo("MTIz")
        assertThat(pdfReq.sporsmal.filter { it.svartype == Svartype.KVITTERING }).isEmpty()

        val journalreq = behandleJournalV2.sisteJournalfoerInngaaendeHenvendelseRequest
        assertThat(journalreq!!.journalpost.dokumentinfoRelasjon.first().journalfoertDokument.dokumentType.value).isEqualTo("NAV 08-14.01")
    }

    @Test
    fun `Reisetilskudd for kode 6 går til Vikafossen`() {
        personMock.returnerKode6 = true

        val aktorId = "aktor"
        val fnr = "fnr"
        whenever(aktorConsumer.finnFnr(aktorId)).thenReturn(fnr)
        val saksId = "saksId"
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)

        whenever(flexBucketUploaderClient.hentVedlegg(any())).thenReturn("123".encodeToByteArray())
        val oppgaveID = 3
        whenever(oppgaveConsumer.opprettOppgave(any())).thenReturn(OppgaveResponse(id = oppgaveID))

        val soknad = OBJECT_MAPPER.readValue(
            TestApplication::class.java.getResource("/reisetilskuddAlleSvar.json"),
            DeprecatedSykepengesoknadDTO::class.java
        ).copy(id = UUID.randomUUID().toString())

        soknadSendtListener.listen(skapConsumerRecord(soknad.id!!, soknad), acknowledgment)

        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        verify(oppgaveConsumer).opprettOppgave(captor.capture())

        val oppgaveRequest = captor.firstValue

        assertThat(oppgaveRequest.tildeltEnhetsnr).isEqualTo("2103")

        personMock.returnerKode6 = false
    }
}

package no.nav.syfo.saksbehandling

import com.nhaarman.mockitokotlin2.*
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.TestApplication
import no.nav.syfo.client.pdl.PdlClient
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
import no.nav.syfo.kafka.consumer.SYKEPENGESOKNAD_TOPIC
import no.nav.syfo.kafka.felles.*
import no.nav.syfo.mock.BehandleJournalMock
import no.nav.syfo.mockReisetilskuddDTO
import no.nav.syfo.mockSykepengesoknadDTO
import no.nav.syfo.serialisertTilString
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
@EnableMockOAuth2Server
class SaksbehandlingIntegrationTest : AbstractContainerBaseTest() {

    @MockBean
    private lateinit var sakConsumer: SakConsumer

    @MockBean
    private lateinit var pdfConsumer: PDFConsumer

    @MockBean
    private lateinit var oppgaveConsumer: OppgaveConsumer

    @MockBean
    private lateinit var flexBucketUploaderClient: FlexBucketUploaderClient

    @Autowired
    private lateinit var pdlClient: PdlClient

    @Autowired
    private lateinit var behandleJournalV2: BehandleJournalMock

    @Autowired
    private lateinit var innsendingDAO: InnsendingDAO

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @Test
    fun `test happycase`() {
        val aktorId = "298374918"
        val saksId = "saksId"
        val oppgaveID = 1
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)
        whenever(oppgaveConsumer.opprettOppgave(any())).thenReturn(OppgaveResponse(id = oppgaveID))

        val soknad = mockSykepengesoknadDTO.copy(
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
            fnr = "fnr"
        )

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10)).until {
            innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id) != null
        }

        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        verify(oppgaveConsumer, never()).opprettOppgave(captor.capture())

        val innsendingIDatabase = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id)!!
        assertThat(innsendingIDatabase.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isNull()
        assertThat(innsendingIDatabase.behandlet).isNotNull()
        verify(pdfConsumer).getPDF(any(), eq(PDFTemplate.ARBEIDSTAKERE))

        val journalreq = behandleJournalV2.sisteJournalfoerInngaaendeHenvendelseRequest
        assertThat(journalreq!!.journalpost.dokumentinfoRelasjon.first().journalfoertDokument.dokumentType.value).isEqualTo("NAV 08-07.04 D")
    }

    @Test
    fun `kafkamelding med redusertVenteperiode setter riktig behandlingstema`() {
        val aktorId = "298374918"
        val saksId = "saksId"
        val oppgaveID = 2
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)
        whenever(oppgaveConsumer.opprettOppgave(any())).thenReturn(OppgaveResponse(id = oppgaveID))

        val soknad = SykepengesoknadDTO(
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
            fnr = "fnr",
            harRedusertVenteperiode = true
        )

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10)).until {
            innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id)?.oppgaveId != null
        }

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

        val innsendingIDatabase = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id)!!
        assertThat(innsendingIDatabase.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isEqualTo(oppgaveID.toString())
        assertThat(innsendingIDatabase.behandlet).isNotNull()
        verify(pdfConsumer).getPDF(any(), eq(PDFTemplate.SELVSTENDIGNAERINGSDRIVENDE))
    }

    @Test
    fun `reisetilskudd søknad behandles korrekt`() {
        val aktorId = "298374918"
        val saksId = "saksId"
        val oppgaveID = 3
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)
        whenever(flexBucketUploaderClient.hentVedlegg(any())).thenReturn("123".encodeToByteArray())
        whenever(oppgaveConsumer.opprettOppgave(any())).thenReturn(OppgaveResponse(id = oppgaveID))

        val soknad = mockReisetilskuddDTO.copy(id = UUID.randomUUID().toString())

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10)).until {
            innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id)?.oppgaveId != null
        }

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

        val innsendingIDatabase = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id)!!
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
    fun `gradert reisetilskudd søknad behandles korrekt`() {
        val aktorId = "298374918"
        val saksId = "saksId"
        val oppgaveID = 3
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)
        whenever(flexBucketUploaderClient.hentVedlegg(any())).thenReturn("123".encodeToByteArray())
        whenever(oppgaveConsumer.opprettOppgave(any())).thenReturn(OppgaveResponse(id = oppgaveID))

        val soknad = mockReisetilskuddDTO.copy(id = UUID.randomUUID().toString(), type = SoknadstypeDTO.GRADERT_REISETILSKUDD)

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10)).until {
            innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id)?.oppgaveId != null
        }

        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        verify(oppgaveConsumer).opprettOppgave(captor.capture())

        val oppgaveRequest = captor.firstValue
        assertThat(oppgaveRequest.aktoerId).isEqualTo(aktorId)
        assertThat(oppgaveRequest.journalpostId).isEqualTo("journalpostId")
        assertThat(oppgaveRequest.saksreferanse).isEqualTo(saksId)
        assertThat(oppgaveRequest.beskrivelse).isEqualTo(
            """
Søknad om sykepenger med reisetilskudd for perioden 18.03.2021 - 22.03.2021

Søknaden har vedlagt 2 kvitteringer med en sum på 1 338,00 kr

Arbeidsgiver: Barnehagen
Organisasjonsnummer: 123454543

Periode 1:
18.03.2021 - 22.03.2021
Grad: 0

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

        val innsendingIDatabase = innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id)!!
        assertThat(innsendingIDatabase.ressursId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isEqualTo(oppgaveID.toString())
        assertThat(innsendingIDatabase.behandlet).isNotNull()

        val pdfReqCaptor: KArgumentCaptor<Soknad> = argumentCaptor()
        verify(pdfConsumer).getPDF(pdfReqCaptor.capture(), eq(PDFTemplate.GRADERT_REISETILSKUDD))

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
        val aktorId = "298374918"
        val saksId = "saksId"
        val oppgaveID = 4
        whenever(sakConsumer.opprettSak(aktorId)).thenReturn(saksId)
        whenever(flexBucketUploaderClient.hentVedlegg(any())).thenReturn("123".encodeToByteArray())
        whenever(oppgaveConsumer.opprettOppgave(any())).thenReturn(OppgaveResponse(id = oppgaveID))
        pdlClient.returnerKode6 = true

        val soknad = mockReisetilskuddDTO

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10)).until {
            innsendingDAO.finnInnsendingForSykepengesoknad(soknad.id)?.oppgaveId != null
        }

        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        verify(oppgaveConsumer).opprettOppgave(captor.capture())
        verify(pdfConsumer).getPDF(any(), eq(PDFTemplate.REISETILSKUDD))

        val oppgaveRequest = captor.firstValue

        assertThat(oppgaveRequest.tildeltEnhetsnr).isEqualTo("2103")

        pdlClient.returnerKode6 = false
    }
}

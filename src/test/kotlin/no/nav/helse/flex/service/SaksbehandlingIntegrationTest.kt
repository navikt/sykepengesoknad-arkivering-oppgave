package no.nav.helse.flex.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.*
import no.nav.helse.flex.domain.*
import no.nav.helse.flex.domain.dto.Svartype
import no.nav.helse.flex.kafka.consumer.SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.sykepengesoknad.kafka.*
import okhttp3.mockwebserver.MockResponse
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

class SaksbehandlingIntegrationTest : FellesTestoppsett() {
    @Autowired
    private lateinit var innsendingRepository: InnsendingRepository

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @Test
    fun `test happycase`() {
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
            innsendingRepository.findBySykepengesoknadId(soknad.id) != null
        }

        val pdfRequest = pdfMockWebserver.takeRequest(10, TimeUnit.SECONDS)!!
        pdfRequest.requestLine shouldBeEqualTo "POST /api/v1/genpdf/syfosoknader/arbeidstakere HTTP/1.1"
        val pdfRequestBody = objectMapper.readValue<Soknad>(pdfRequest.body.readUtf8())
        pdfRequestBody.soknadPerioder!!.first().sykmeldingstype shouldBeEqualTo "AKTIVITET_IKKE_MULIG"

        val innsendingIDatabase = innsendingRepository.findBySykepengesoknadId(soknad.id)!!
        assertThat(innsendingIDatabase.sykepengesoknadId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isNull()
        assertThat(innsendingIDatabase.behandlet).isNotNull
    }

    @Test
    fun `kafkamelding med redusertVenteperiode setter riktig behandlingstema`() {
        val oppgaveID = 2
        oppgaveMockWebserver.enqueue(
            MockResponse().setBody(
                OppgaveResponse(
                    oppgaveID,
                    "4488",
                    "SYK",
                    "SOK"
                ).serialisertTilString()
            ).addHeader("Content-Type", "application/json")
        )

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
            innsendingRepository.findBySykepengesoknadId(soknad.id)?.oppgaveId != null
        }

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")

        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.journalpostId).isEqualTo("journalpostId")
        assertThat(oppgaveRequestBody.beskrivelse).isEqualTo(
            """
Søknad om sykepenger fra Selvstendig Næringsdrivende / Frilanser for perioden 01.05.2020 - 05.05.2020

Har systemet gode integrasjonstester?
Ja
            """.trimIndent()
        )
        assertThat(oppgaveRequestBody.tema).isEqualTo("SYK")
        assertThat(oppgaveRequestBody.oppgavetype).isEqualTo("SOK")
        assertThat(oppgaveRequestBody.prioritet).isEqualTo("NORM")
        assertThat(oppgaveRequestBody.behandlingstema).isNull()
        assertThat(oppgaveRequestBody.behandlingstype).isEqualTo("ae0247")

        val innsendingIDatabase = innsendingRepository.findBySykepengesoknadId(soknad.id)!!
        assertThat(innsendingIDatabase.sykepengesoknadId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isEqualTo(oppgaveID.toString())
        assertThat(innsendingIDatabase.behandlet).isNotNull

        val pdfRequest = pdfMockWebserver.takeRequest(10, TimeUnit.SECONDS)!!
        pdfRequest.requestLine shouldBeEqualTo "POST /api/v1/genpdf/syfosoknader/selvstendignaeringsdrivende HTTP/1.1"
    }

    @Test
    fun `reisetilskudd søknad behandles korrekt`() {
        val oppgaveID = 3
        oppgaveMockWebserver.enqueue(
            MockResponse().setBody(
                OppgaveResponse(
                    oppgaveID,
                    "4488",
                    "SYK",
                    "SOK"
                ).serialisertTilString()
            ).addHeader("Content-Type", "application/json")
        )

        val soknad = mockReisetilskuddDTO.copy(id = UUID.randomUUID().toString())

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10)).until {
            innsendingRepository.findBySykepengesoknadId(soknad.id)?.oppgaveId != null
        }

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")

        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.journalpostId).isEqualTo("journalpostId")
        assertThat(oppgaveRequestBody.beskrivelse).isEqualTo(
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
        assertThat(oppgaveRequestBody.tema).isEqualTo("SYK")
        assertThat(oppgaveRequestBody.oppgavetype).isEqualTo("SOK")
        assertThat(oppgaveRequestBody.prioritet).isEqualTo("NORM")
        assertThat(oppgaveRequestBody.behandlingstema).isEqualTo("ab0237")
        assertThat(oppgaveRequestBody.behandlingstype).isNull()
        assertThat(oppgaveRequestBody.tildeltEnhetsnr).isEqualTo(null)

        val innsendingIDatabase = innsendingRepository.findBySykepengesoknadId(soknad.id)!!
        assertThat(innsendingIDatabase.sykepengesoknadId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isEqualTo(oppgaveID.toString())
        assertThat(innsendingIDatabase.behandlet).isNotNull

        val pdfRequest = pdfMockWebserver.takeRequest(10, TimeUnit.SECONDS)!!
        pdfRequest.requestLine shouldBeEqualTo "POST /api/v1/genpdf/syfosoknader/reisetilskudd HTTP/1.1"
        val pdfRequestBody = objectMapper.readValue<Soknad>(pdfRequest.body.readUtf8())
        pdfRequestBody.kvitteringSum shouldBeEqualTo 133800
        pdfRequestBody.kvitteringer!!.size shouldBeEqualTo 2
        pdfRequestBody.kvitteringer!!.first().b64data shouldBeEqualTo "MTIz"
        pdfRequestBody.sporsmal.none { it.svartype == Svartype.KVITTERING } shouldBeEqualTo true
        pdfRequestBody.soknadPerioder!!.first().sykmeldingstype shouldBeEqualTo "REISETILSKUDD"
    }

    @Test
    fun `gradert reisetilskudd søknad behandles korrekt`() {
        val oppgaveID = 3
        oppgaveMockWebserver.enqueue(
            MockResponse().setBody(
                OppgaveResponse(
                    oppgaveID,
                    "4488",
                    "SYK",
                    "SOK"
                ).serialisertTilString()
            ).addHeader("Content-Type", "application/json")
        )

        val soknad = mockReisetilskuddDTO.copy(
            id = UUID.randomUUID().toString(),
            type = SoknadstypeDTO.GRADERT_REISETILSKUDD
        )

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10)).until {
            innsendingRepository.findBySykepengesoknadId(soknad.id)?.oppgaveId != null
        }

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")

        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.journalpostId).isEqualTo("journalpostId")
        assertThat(oppgaveRequestBody.beskrivelse).isEqualTo(
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
        assertThat(oppgaveRequestBody.tema).isEqualTo("SYK")
        assertThat(oppgaveRequestBody.oppgavetype).isEqualTo("SOK")
        assertThat(oppgaveRequestBody.prioritet).isEqualTo("NORM")
        assertThat(oppgaveRequestBody.behandlingstema).isEqualTo("ab0237")
        assertThat(oppgaveRequestBody.behandlingstype).isNull()
        assertThat(oppgaveRequestBody.tildeltEnhetsnr).isEqualTo(null)

        val innsendingIDatabase = innsendingRepository.findBySykepengesoknadId(soknad.id)!!
        assertThat(innsendingIDatabase.sykepengesoknadId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isEqualTo(oppgaveID.toString())
        assertThat(innsendingIDatabase.behandlet).isNotNull

        val pdfRequest = pdfMockWebserver.takeRequest(10, TimeUnit.SECONDS)!!
        pdfRequest.requestLine shouldBeEqualTo "POST /api/v1/genpdf/syfosoknader/gradertreisetilskudd HTTP/1.1"
        val pdfRequestBody = objectMapper.readValue<Soknad>(pdfRequest.body.readUtf8())
        pdfRequestBody.kvitteringSum shouldBeEqualTo 133800
        pdfRequestBody.kvitteringer!!.size shouldBeEqualTo 2
        pdfRequestBody.kvitteringer!!.first().b64data shouldBeEqualTo "MTIz"
        pdfRequestBody.sporsmal.none { it.svartype == Svartype.KVITTERING } shouldBeEqualTo true
        pdfRequestBody.soknadPerioder!!.first().sykmeldingstype shouldBeEqualTo "REISETILSKUDD"
    }
}

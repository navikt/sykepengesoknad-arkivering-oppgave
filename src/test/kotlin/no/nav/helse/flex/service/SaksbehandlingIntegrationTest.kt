package no.nav.helse.flex.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.*
import no.nav.helse.flex.domain.*
import no.nav.helse.flex.domain.dto.Svartype
import no.nav.helse.flex.kafka.consumer.SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.mockdispatcher.SykepengesoknadMockDispatcher
import no.nav.helse.flex.sykepengesoknad.kafka.*
import okhttp3.mockwebserver.MockResponse
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

class SaksbehandlingIntegrationTest : FellesTestOppsett() {
    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @Test
    fun `test arbeidstaker`() {
        val soknad =
            mockSykepengesoknadDTO.copy(
                id = UUID.randomUUID().toString(),
                opprettet = LocalDateTime.now(),
                fom = LocalDate.of(2019, 5, 4),
                tom = LocalDate.of(2019, 5, 8),
                type = SoknadstypeDTO.ARBEIDSTAKERE,
                sporsmal =
                    listOf(
                        SporsmalDTO(
                            id = UUID.randomUUID().toString(),
                            tag = "TAGGEN",
                            sporsmalstekst = "Har systemet gode integrasjonstester?",
                            svartype = SvartypeDTO.JA_NEI,
                            svar = listOf(SvarDTO(verdi = "JA")),
                        ),
                    ),
                status = SoknadsstatusDTO.SENDT,
                sendtNav = LocalDateTime.now(),
                fnr = "fnr",
            )

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString(),
            ),
        )

        await().atMost(Duration.ofSeconds(10)).until {
            innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet != null
        }

        val pdfRequest = pdfMockWebserver.takeRequest(10, TimeUnit.SECONDS)!!
        pdfRequest.requestLine shouldBeEqualTo "POST /api/v1/genpdf/syfosoknader/arbeidstakere HTTP/1.1"
        val pdfRequestBody = objectMapper.readValue<Soknad>(pdfRequest.body.readUtf8())
        pdfRequestBody.soknadPerioder!!.first().sykmeldingstype shouldBeEqualTo "AKTIVITET_IKKE_MULIG"

        val innsendingIDatabase = innsendingRepository.findBySykepengesoknadId(soknad.id)!!
        assertThat(innsendingIDatabase.sykepengesoknadId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isNull()
        assertThat(innsendingIDatabase.behandlet).isNotNull

        val dokArkivRequest = dokArkivMockWebserver.takeRequest(1, TimeUnit.SECONDS)!!
        dokArkivRequest.requestLine shouldBeEqualTo "POST /rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true HTTP/1.1"
        val dokArkivRequestBody = objectMapper.readValue<JournalpostRequest>(dokArkivRequest.body.readUtf8())
        dokArkivRequestBody.dokumenter[0].tittel `should be equal to` "Søknad om sykepenger for perioden 04.05.2019 til 08.05.2019"

        // Lar den time ut
        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(10L, ChronoUnit.DAYS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")

        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.journalpostId).isEqualTo("journalpostId")

        assertThat(oppgaveRequestBody.tema).isEqualTo("SYK")

        assertThat(oppgaveRequestBody.oppgavetype).isEqualTo("SOK")
        assertThat(oppgaveRequestBody.prioritet).isEqualTo("NORM")
        assertThat(oppgaveRequestBody.behandlingstema).isEqualTo("ab0061")
        assertThat(oppgaveRequestBody.behandlingstype).isNull()
    }

    @Test
    fun `kafkamelding med redusertVenteperiode setter riktig behandlingstema`() {
        val oppgaveID = 2
        oppgaveMockWebserver.enqueue(
            MockResponse()
                .setBody(
                    OpprettOppgaveResponse(
                        oppgaveID,
                        "4488",
                        "SYK",
                        "SOK",
                    ).serialisertTilString(),
                ).addHeader("Content-Type", "application/json"),
        )

        val soknad =
            SykepengesoknadDTO(
                id = UUID.randomUUID().toString(),
                opprettet = LocalDateTime.now(),
                fom = LocalDate.of(2020, 5, 1),
                tom = LocalDate.of(2020, 5, 5),
                type = SoknadstypeDTO.SELVSTENDIGE_OG_FRILANSERE,
                arbeidssituasjon = ArbeidssituasjonDTO.FRILANSER,
                sporsmal =
                    listOf(
                        SporsmalDTO(
                            id = UUID.randomUUID().toString(),
                            tag = "TAGGEN",
                            sporsmalstekst = "Har systemet gode integrasjonstester?",
                            svartype = SvartypeDTO.JA_NEI,
                            svar = listOf(SvarDTO(verdi = "JA")),
                        ),
                    ),
                status = SoknadsstatusDTO.SENDT,
                sendtNav = LocalDateTime.now(),
                fnr = "fnr",
                harRedusertVenteperiode = true,
            )

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString(),
            ),
        )

        await().atMost(Duration.ofSeconds(10)).until {
            innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet != null
        }
        SykepengesoknadMockDispatcher.enque(soknad)
        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")

        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.journalpostId).isEqualTo("journalpostId")
        assertThat(oppgaveRequestBody.beskrivelse).isEqualTo(
            """
            Søknad om sykepenger for frilanser for perioden 01.05.2020 til 05.05.2020

            Har systemet gode integrasjonstester?
            Ja
            """.trimIndent(),
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

        val dokArkivRequest = dokArkivMockWebserver.takeRequest(1, TimeUnit.SECONDS)!!
        dokArkivRequest.requestLine shouldBeEqualTo "POST /rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true HTTP/1.1"
        val dokArkivRequestBody = objectMapper.readValue<JournalpostRequest>(dokArkivRequest.body.readUtf8())
        dokArkivRequestBody.dokumenter[0].tittel `should be equal to`
            "Søknad om sykepenger for frilanser for perioden 01.05.2020 til 05.05.2020"

        val pdlRequest = pdlMockWebserver.takeRequest(1, TimeUnit.SECONDS)!!
        pdlRequest.headers["Behandlingsnummer"] `should be equal to` "B128"
        pdlRequest.headers["Tema"] `should be equal to` "SYK"
    }

    @Test
    fun `reisetilskudd søknad behandles korrekt`() {
        val oppgaveID = 3
        oppgaveMockWebserver.enqueue(
            MockResponse()
                .setBody(
                    OpprettOppgaveResponse(
                        oppgaveID,
                        "4488",
                        "SYK",
                        "SOK",
                    ).serialisertTilString(),
                ).addHeader("Content-Type", "application/json"),
        )

        val soknad = mockReisetilskuddDTO.copy(id = UUID.randomUUID().toString())

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString(),
            ),
        )

        await().atMost(Duration.ofSeconds(10)).until {
            innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet != null
        }
        SykepengesoknadMockDispatcher.enque(soknad)

        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))
        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")

        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.journalpostId).isEqualTo("journalpostId")
        assertThat(oppgaveRequestBody.beskrivelse).isEqualTo(
            """
            Søknad om reisetilskudd for perioden 18.03.2021 til 22.03.2021

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
            """.trimIndent(),
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

        val dokArkivRequest = dokArkivMockWebserver.takeRequest(1, TimeUnit.SECONDS)!!
        dokArkivRequest.requestLine shouldBeEqualTo "POST /rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true HTTP/1.1"
        val dokArkivRequestBody = objectMapper.readValue<JournalpostRequest>(dokArkivRequest.body.readUtf8())
        dokArkivRequestBody.dokumenter[0].tittel `should be equal to` "Søknad om reisetilskudd for perioden 18.03.2021 til 22.03.2021"
    }

    @Test
    fun `behandlingsdager søknad behandles korrekt`() {
        val oppgaveID = 99
        oppgaveMockWebserver.enqueue(
            MockResponse()
                .setBody(
                    OpprettOppgaveResponse(
                        oppgaveID,
                        "4488",
                        "SYK",
                        "SOK",
                    ).serialisertTilString(),
                ).addHeader("Content-Type", "application/json"),
        )

        val soknad = mockBehandlingsdagerdDTO.copy(id = UUID.randomUUID().toString())

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString(),
            ),
        )

        await().atMost(Duration.ofSeconds(20)).until {
            innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet != null
        }

        SykepengesoknadMockDispatcher.enque(soknad)
        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")

        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.journalpostId).isEqualTo("journalpostId")
        assertThat(oppgaveRequestBody.beskrivelse).isEqualTo(
            """
            Søknad om enkeltstående behandlingsdager for arbeidsledig for perioden 02.10.2023 til 15.10.2023

            Periode 1:
            02.10.2023 - 15.10.2023

            Hvilke dager kunne du ikke være arbeidssøker på grunn av behandling mellom 2. - 15. oktober 2023?
                04.10.2023

                11.10.2023
            """.trimIndent(),
        )
        assertThat(oppgaveRequestBody.tema).isEqualTo("SYK")
        assertThat(oppgaveRequestBody.oppgavetype).isEqualTo("SOK")
        assertThat(oppgaveRequestBody.prioritet).isEqualTo("NORM")
        assertThat(oppgaveRequestBody.behandlingstema).isEqualTo("ab0351")
        assertThat(oppgaveRequestBody.behandlingstype).isNull()
        assertThat(oppgaveRequestBody.tildeltEnhetsnr).isEqualTo(null)

        val innsendingIDatabase = innsendingRepository.findBySykepengesoknadId(soknad.id)!!
        assertThat(innsendingIDatabase.sykepengesoknadId).isEqualTo(soknad.id)
        assertThat(innsendingIDatabase.oppgaveId).isEqualTo(oppgaveID.toString())
        assertThat(innsendingIDatabase.behandlet).isNotNull

        val pdfRequest = pdfMockWebserver.takeRequest(10, TimeUnit.SECONDS)!!
        pdfRequest.requestLine shouldBeEqualTo "POST /api/v1/genpdf/syfosoknader/behandlingsdager HTTP/1.1"

        val dokArkivRequestJournalpostRequest = dokArkivMockWebserver.takeRequest(1, TimeUnit.SECONDS)!!
        dokArkivRequestJournalpostRequest.requestLine shouldBeEqualTo
            "POST /rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true HTTP/1.1"

        val dokArkivRequestJournalpostBody =
            objectMapper.readValue<JournalpostRequest>(dokArkivRequestJournalpostRequest.body.readUtf8())
        dokArkivRequestJournalpostBody.dokumenter[0].tittel `should be equal to`
            "Søknad om enkeltstående behandlingsdager for arbeidsledig for perioden 02.10.2023 til 15.10.2023"

        val dokArkivLogiskVedleggRequest = dokArkivMockWebserver.takeRequest(1, TimeUnit.SECONDS)!!
        dokArkivLogiskVedleggRequest.requestLine shouldBeEqualTo "POST /rest/journalpostapi/v1/dokumentInfo/123456/logiskVedlegg/ HTTP/1.1"

        val dokArkivLogiskVedleggRequestBody =
            objectMapper.readValue<LogiskVedleggRequest>(dokArkivLogiskVedleggRequest.body.readUtf8())
        dokArkivLogiskVedleggRequestBody.tittel `should be equal to` "2 behandlingsdager, 041023, 111023 / 1 egenmeldingsdager, 031023"
    }

    @Test
    fun `gradert reisetilskudd søknad behandles korrekt`() {
        val oppgaveID = 3
        oppgaveMockWebserver.enqueue(
            MockResponse()
                .setBody(
                    OpprettOppgaveResponse(
                        oppgaveID,
                        "4488",
                        "SYK",
                        "SOK",
                    ).serialisertTilString(),
                ).addHeader("Content-Type", "application/json"),
        )

        val soknad =
            mockReisetilskuddDTO.copy(
                id = UUID.randomUUID().toString(),
                type = SoknadstypeDTO.GRADERT_REISETILSKUDD,
            )

        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                soknad.id,
                soknad.serialisertTilString(),
            ),
        )

        await().atMost(Duration.ofSeconds(10)).until {
            innsendingRepository.findBySykepengesoknadId(soknad.id)?.behandlet != null
        }

        SykepengesoknadMockDispatcher.enque(soknad)
        oppgaveOpprettelse.behandleOppgaver(Instant.now().plus(1L, ChronoUnit.HOURS))

        val oppgaveRequest = oppgaveMockWebserver.takeRequest(5, TimeUnit.SECONDS)!!
        assertThat(oppgaveRequest.requestLine).isEqualTo("POST /api/v1/oppgaver HTTP/1.1")

        val oppgaveRequestBody = objectMapper.readValue<OppgaveRequest>(oppgaveRequest.body.readUtf8())
        assertThat(oppgaveRequestBody.journalpostId).isEqualTo("journalpostId")
        assertThat(oppgaveRequestBody.beskrivelse).isEqualTo(
            """
            Søknad om sykepenger med reisetilskudd for perioden 18.03.2021 til 22.03.2021

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
            """.trimIndent(),
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

        val dokArkivRequest = dokArkivMockWebserver.takeRequest(1, TimeUnit.SECONDS)!!
        dokArkivRequest.requestLine shouldBeEqualTo "POST /rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true HTTP/1.1"
        val dokArkivRequestBody = objectMapper.readValue<JournalpostRequest>(dokArkivRequest.body.readUtf8())
        dokArkivRequestBody.dokumenter[0].tittel `should be equal to` "Søknad om sykepenger med reisetilskudd for " +
            "perioden 18.03.2021 til 22.03.2021"
    }
}

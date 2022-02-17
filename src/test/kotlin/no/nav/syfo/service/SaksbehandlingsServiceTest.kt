package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.any
import no.nav.syfo.arkivering.Arkivaren
import no.nav.syfo.client.FlexBucketUploaderClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.producer.RebehandleSykepengesoknadProducer
import no.nav.syfo.repository.InnsendingDbRecord
import no.nav.syfo.repository.InnsendingRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SaksbehandlingsServiceTest {
    @Mock
    lateinit var pdlClient: PdlClient
    @Mock
    lateinit var identService: IdentService
    @Mock
    lateinit var innsendingRepository: InnsendingRepository
    @Mock
    lateinit var oppgaveService: OppgaveService
    @Mock
    lateinit var arkivaren: Arkivaren
    @Mock
    lateinit var flexBucketUploaderClient: FlexBucketUploaderClient
    @Mock
    lateinit var registry: MeterRegistry
    @Mock
    lateinit var rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer

    @InjectMocks
    lateinit var saksbehandlingsService: SaksbehandlingsService

    private val objectMapper = ObjectMapper().registerKotlinModule().registerModules(JavaTimeModule())
    private val aktorId = "aktorId-745463060"
    private val fnr = "12345678901"

    @BeforeEach
    fun setup() {
        given(pdlClient.hentFormattertNavn(any())).willReturn("Personnavn")
        given(identService.hentAktorIdForFnr(any())).willReturn(aktorId)
        given(identService.hentFnrForAktorId(any())).willReturn(fnr)
        given(arkivaren.opprettJournalpost(any())).willReturn("journalpostId")
        given(oppgaveService.opprettOppgave(any())).willReturn(OppgaveResponse(1234))
        given(
            registry.counter(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyIterable()
            )
        ).willReturn(mock(Counter::class.java))
        given(innsendingRepository.save(any())).willReturn(
            InnsendingDbRecord(
                id = UUID.randomUUID().toString(),
                sykepengesoknadId = "innsending-guid"
            )
        )
    }

    @Test
    fun behandlerSoknaderSomEttersendesTilNavDerDetManglerOppgave() {
        val now = LocalDateTime.now()
        val sykepengesoknadUtenOppgave = objectMapper.readValue(SaksbehandlingsServiceTest::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
            .copy(sendtNav = null, sendtArbeidsgiver = now)
        val sykepengesoknadEttersendingTilNAV = objectMapper.readValue(SaksbehandlingsServiceTest::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
            .copy(sendtNav = now, sendtArbeidsgiver = now)

        saksbehandlingsService.behandleSoknad(sykepengesoknadUtenOppgave)
        verify(arkivaren, times(1)).opprettJournalpost(any())
        verify(oppgaveService, never()).opprettOppgave(any())
        given(innsendingRepository.findBySykepengesoknadId(sykepengesoknadUtenOppgave.id)).willReturn(
            innsending(sykepengesoknadUtenOppgave)
        )

        saksbehandlingsService.behandleSoknad(sykepengesoknadEttersendingTilNAV)
        saksbehandlingsService.opprettOppgave(sykepengesoknadEttersendingTilNAV, innsending(sykepengesoknadEttersendingTilNAV))
        verify(arkivaren, times(1)).opprettJournalpost(any())
        verify(oppgaveService, times(1)).opprettOppgave(any())
    }

    private fun innsending(
        sykepengesoknadUtenOppgave: Sykepengesoknad
    ): InnsendingDbRecord {
        return InnsendingDbRecord(
            id = "innsending-guid",
            sykepengesoknadId = sykepengesoknadUtenOppgave.id,
            journalpostId = "journalpostId",
            oppgaveId = null,
            behandlet = Instant.now()
        )
    }

    @Test
    fun `oppretter oppgave med korrekte felter`() {
        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        val søknad = objectMapper.readValue(SaksbehandlingsServiceTest::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
        saksbehandlingsService.opprettOppgave(søknad, innsending(søknad))
        verify(oppgaveService).opprettOppgave(captor.capture())
        val oppgaveRequest = captor.firstValue
        Assertions.assertThat(oppgaveRequest.journalpostId).isEqualTo("journalpostId")
        Assertions.assertThat(oppgaveRequest.beskrivelse).isEqualTo(
            """
Søknad om sykepenger for perioden 16.10.2018 - 24.10.2018

Arbeidsgiver: ARBEIDSGIVER A/S
Organisasjonsnummer: 1257358

Periode 1:
16.10.2018 - 20.10.2018
Grad: 100

Periode 2:
21.10.2018 - 24.10.2018
Grad: 40

Betaler arbeidsgiveren lønnen din når du er syk?
Vet ikke
            """.trimIndent()
        )
        Assertions.assertThat(oppgaveRequest.tema).isEqualTo("SYK")
        Assertions.assertThat(oppgaveRequest.oppgavetype).isEqualTo("SOK")
        Assertions.assertThat(oppgaveRequest.prioritet).isEqualTo("NORM")
        Assertions.assertThat(oppgaveRequest.behandlingstema).isEqualTo("ab0061")
    }
}

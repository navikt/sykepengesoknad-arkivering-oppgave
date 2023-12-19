package no.nav.helse.flex.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.repository.InnsendingDbRecord
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.spreoppgave.HandterOppave
import no.nav.helse.flex.spreoppgave.SpreOppgaverService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class SpreOppgaverServiceTest {
    @Mock
    lateinit var saksbehandlingsService: SaksbehandlingsService

    lateinit var spreOppgaverService: SpreOppgaverService
    lateinit var handterOppave: HandterOppave

    @Mock
    lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @Mock
    lateinit var registry: MeterRegistry

    @Mock
    lateinit var counter: Counter

    private val objectMapper = ObjectMapper().registerKotlinModule().registerModules(JavaTimeModule())
    private val sok =
        objectMapper.readValue(
            SpreOppgaverServiceTest::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"),
            Sykepengesoknad::class.java,
        )
    private val now = LocalDateTime.now()

    private fun innsending(soknadsId: String) =
        InnsendingDbRecord(
            "id",
            sykepengesoknadId = soknadsId,
            journalpostId = "journalpost",
            oppgaveId = null,
            behandlet = null,
        )

    @BeforeEach
    fun setup() {
        whenever(registry.counter(any())).thenReturn(counter)
        handterOppave = HandterOppave(spreOppgaveRepository, registry)
        spreOppgaverService = SpreOppgaverService(saksbehandlingsService, spreOppgaveRepository, handterOppave)
    }

    @Test
    fun ignorererSoknaderSomErNY() {
        val sykepengesoknad =
            sok
                .copy(status = "NY", sendtNav = null, sendtArbeidsgiver = null)

        spreOppgaverService.soknadSendt(sykepengesoknad)

        verify(saksbehandlingsService, never()).behandleSoknad(any())
    }

    @Test
    fun ignorererSoknaderSomErFREMTIDIG() {
        val sykepengesoknad =
            sok
                .copy(status = "FREMTIDIG", sendtNav = null, sendtArbeidsgiver = null)

        spreOppgaverService.soknadSendt(sykepengesoknad)

        verify(saksbehandlingsService, never()).behandleSoknad(any())
    }

    @Test
    fun ignorererEttersendingTilArbeidsgiver() {
        val sykepengesoknad =
            sok
                .copy(sendtNav = now, sendtArbeidsgiver = now.plusMinutes(20))

        spreOppgaverService.soknadSendt(sykepengesoknad)
        verify(saksbehandlingsService, never()).behandleSoknad(sykepengesoknad)
    }

    @Test
    fun oppretterIkkeOppgaveForSoknadSomIkkeErSendtTilNAV() {
        val sykepengesoknad =
            sok
                .copy(sendtNav = null, sendtArbeidsgiver = LocalDateTime.now())

        spreOppgaverService.soknadSendt(sykepengesoknad)
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
    }

    @Test
    fun behandlerEttersendingTilNAV() {
        val sykepengesoknad =
            sok
                .copy(sendtNav = now.plusHours(1), sendtArbeidsgiver = now)

        spreOppgaverService.soknadSendt(sykepengesoknad)
        verify(saksbehandlingsService, times(1)).behandleSoknad(sykepengesoknad)
        verify(saksbehandlingsService, never()).opprettOppgave(sykepengesoknad, innsending(sykepengesoknad.id))
    }

    @Test
    fun behandlerSoknadSomSkalTilArbeidsgiverOgNav() {
        val sykepengesoknad =
            sok
                .copy(sendtNav = now, sendtArbeidsgiver = now)

        spreOppgaverService.soknadSendt(sykepengesoknad)
        verify(saksbehandlingsService, times(1)).behandleSoknad(sykepengesoknad)
        verify(saksbehandlingsService, never()).opprettOppgave(sykepengesoknad, innsending(sykepengesoknad.id))
    }
}

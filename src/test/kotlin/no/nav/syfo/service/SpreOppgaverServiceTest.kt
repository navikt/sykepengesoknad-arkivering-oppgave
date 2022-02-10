package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.TestApplication
import no.nav.syfo.domain.dto.Arbeidssituasjon
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.repository.InnsendingDbRecord
import no.nav.syfo.repository.OppgavestyringDAO
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

    @Mock
    lateinit var oppgavestyringDAO: OppgavestyringDAO

    @Mock
    lateinit var registry: MeterRegistry

    @Mock
    lateinit var counter: Counter

    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), KotlinModule())
    private val sok = objectMapper.readValue(
        TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"),
        Sykepengesoknad::class.java
    )
    private val now = LocalDateTime.now()
    private fun innsending(soknadsId: String) = InnsendingDbRecord(
        "id",
        sykepengesoknadId = soknadsId,
        journalpostId = "journalpost",
        oppgaveId = null,
        behandlet = null,
    )

    @BeforeEach
    fun setup() {
        whenever(registry.counter(any())).thenReturn(counter)
        spreOppgaverService = SpreOppgaverService("1", saksbehandlingsService, oppgavestyringDAO, registry)
    }

    @Test
    fun ignorererSoknaderSomErNY() {
        val sykepengesoknad = sok
            .copy(status = "NY", sendtNav = null, sendtArbeidsgiver = null)

        spreOppgaverService.soknadSendt(sykepengesoknad)

        verify(saksbehandlingsService, never()).behandleSoknad(any())
    }

    @Test
    fun ignorererSoknaderSomErFREMTIDIG() {
        val sykepengesoknad = sok
            .copy(status = "FREMTIDIG", sendtNav = null, sendtArbeidsgiver = null)

        spreOppgaverService.soknadSendt(sykepengesoknad)

        verify(saksbehandlingsService, never()).behandleSoknad(any())
    }

    @Test
    fun ignorererEttersendingTilArbeidsgiver() {
        val sykepengesoknad = sok
            .copy(sendtNav = now, sendtArbeidsgiver = now.plusMinutes(20))

        spreOppgaverService.soknadSendt(sykepengesoknad)
        verify(saksbehandlingsService, never()).behandleSoknad(sykepengesoknad)
    }

    @Test
    fun oppretterIkkeOppgaveForSoknadSomIkkeErSendtTilNAV() {
        val sykepengesoknad = sok
            .copy(sendtNav = null, sendtArbeidsgiver = LocalDateTime.now())

        spreOppgaverService.soknadSendt(sykepengesoknad)
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
    }

    @Test
    fun behandlerEttersendingTilNAV() {
        val sykepengesoknad = sok
            .copy(sendtNav = now.plusHours(1), sendtArbeidsgiver = now)

        spreOppgaverService.soknadSendt(sykepengesoknad)
        verify(saksbehandlingsService, times(1)).behandleSoknad(sykepengesoknad)
        verify(saksbehandlingsService, never()).opprettOppgave(sykepengesoknad, innsending(sykepengesoknad.id))
    }

    @Test
    fun behandlerSoknadSomSkalTilArbeidsgiverOgNav() {
        val sykepengesoknad = sok
            .copy(sendtNav = now, sendtArbeidsgiver = now)

        spreOppgaverService.soknadSendt(sykepengesoknad)
        verify(saksbehandlingsService, times(1)).behandleSoknad(sykepengesoknad)
        verify(saksbehandlingsService, never()).opprettOppgave(sykepengesoknad, innsending(sykepengesoknad.id))
    }

    @Test
    fun utsetterBareArbeidstakerSoknader() {
        whenever(saksbehandlingsService.finnEksisterendeInnsending(any())).thenAnswer { innsending(it.arguments[0].toString()) }

        val arbeidstaker = objectMapper.readValue(
            TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"),
            Sykepengesoknad::class.java
        )
        val frilanser = arbeidstaker.copy(
            soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE,
            arbeidssituasjon = Arbeidssituasjon.FRILANSER,
            sendtArbeidsgiver = null
        )
        val arbeidsledig = arbeidstaker.copy(
            soknadstype = Soknadstype.ARBEIDSLEDIG,
            arbeidssituasjon = Arbeidssituasjon.ARBEIDSLEDIG,
            sendtArbeidsgiver = null
        )
        val annet = arbeidstaker.copy(
            soknadstype = Soknadstype.ANNET_ARBEIDSFORHOLD,
            arbeidssituasjon = Arbeidssituasjon.ANNET,
            sendtArbeidsgiver = null
        )
        val behandlingsdagerSelvstendig = arbeidstaker.copy(
            soknadstype = Soknadstype.BEHANDLINGSDAGER,
            arbeidssituasjon = Arbeidssituasjon.NAERINGSDRIVENDE,
            sendtArbeidsgiver = null
        )
        val behandlingsdagerArbeidstaker = arbeidstaker.copy(
            soknadstype = Soknadstype.BEHANDLINGSDAGER,
            arbeidssituasjon = Arbeidssituasjon.ARBEIDSTAKER
        )
        var hit = 0

        spreOppgaverService.soknadSendt(arbeidstaker)
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())

        spreOppgaverService.soknadSendt(frilanser)
        verify(saksbehandlingsService, times(++hit)).opprettOppgave(any(), any(), any())

        spreOppgaverService.soknadSendt(arbeidsledig)
        verify(saksbehandlingsService, times(++hit)).opprettOppgave(any(), any(), any())

        spreOppgaverService.soknadSendt(annet)
        verify(saksbehandlingsService, times(++hit)).opprettOppgave(any(), any(), any())

        spreOppgaverService.soknadSendt(behandlingsdagerSelvstendig)
        verify(saksbehandlingsService, times(++hit)).opprettOppgave(any(), any(), any())

        spreOppgaverService.soknadSendt(behandlingsdagerArbeidstaker)
        verify(saksbehandlingsService, times(++hit)).opprettOppgave(any(), any(), any())
    }
}

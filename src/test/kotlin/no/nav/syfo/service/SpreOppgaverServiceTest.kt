package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import no.nav.syfo.TestApplication
import no.nav.syfo.config.unleash.ToggleImpl
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import no.nav.syfo.domain.dto.Arbeidssituasjon
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Sykepengesoknad
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class SpreOppgaverServiceTest {
    @Mock
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @Mock
    lateinit var toggle: ToggleImpl

    @Mock
    lateinit var syfosoknadConsumer: SyfosoknadConsumer

    lateinit var spreOppgaverService: SpreOppgaverService

    @Mock
    lateinit var oppgavestyringDAO: OppgavestyringDAO

    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), KotlinModule())
    private val sok = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
    private val now = LocalDateTime.now()

    @Before
    fun setup() {
        spreOppgaverService = SpreOppgaverService("1", syfosoknadConsumer, toggle, saksbehandlingsService, oppgavestyringDAO)
        whenever(toggle.isNotProduction()).thenReturn(false)
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
        verify(saksbehandlingsService, never()).opprettOppgave(sykepengesoknad)
    }

    @Test
    fun behandlerEttersendingTilNAV() {
        val sykepengesoknad = sok
            .copy(sendtNav = now.plusHours(1), sendtArbeidsgiver = now)

        spreOppgaverService.soknadSendt(sykepengesoknad)
        verify(saksbehandlingsService, times(1)).behandleSoknad(sykepengesoknad)
        verify(saksbehandlingsService, times(1)).opprettOppgave(sykepengesoknad)
    }

    @Test
    fun behandlerSoknadSomSkalTilArbeidsgiverOgNav() {
        val sykepengesoknad = sok
            .copy(sendtNav = now, sendtArbeidsgiver = now)

        spreOppgaverService.soknadSendt(sykepengesoknad)
        verify(saksbehandlingsService, times(1)).behandleSoknad(sykepengesoknad)
        verify(saksbehandlingsService, times(1)).opprettOppgave(sykepengesoknad)
    }

    @Test
    fun utsetterBareArbeidstakerSoknader() {
        val arbeidstaker = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
        val frilanser = arbeidstaker.copy(soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE, arbeidssituasjon = Arbeidssituasjon.FRILANSER, sendtArbeidsgiver = null)
        val arbeidsledig = arbeidstaker.copy(soknadstype = Soknadstype.ARBEIDSLEDIG, arbeidssituasjon = Arbeidssituasjon.ARBEIDSLEDIG, sendtArbeidsgiver = null)
        val annet = arbeidstaker.copy(soknadstype = Soknadstype.ANNET_ARBEIDSFORHOLD, arbeidssituasjon = Arbeidssituasjon.ANNET, sendtArbeidsgiver = null)
        val behandlingsdagerSelvstendig = arbeidstaker.copy(soknadstype = Soknadstype.BEHANDLINGSDAGER, arbeidssituasjon = Arbeidssituasjon.NAERINGSDRIVENDE, sendtArbeidsgiver = null)
        val behandlingsdagerArbeidstaker = arbeidstaker.copy(soknadstype = Soknadstype.BEHANDLINGSDAGER, arbeidssituasjon = Arbeidssituasjon.ARBEIDSTAKER)
        var hit = 0

        spreOppgaverService.soknadSendt(arbeidstaker)
        // TODO: verify(saksbehandlingsService, never()).opprettOppgave(any())
        verify(saksbehandlingsService, times(++hit)).opprettOppgave(any())

        spreOppgaverService.soknadSendt(frilanser)
        verify(saksbehandlingsService, times(++hit)).opprettOppgave(any())

        spreOppgaverService.soknadSendt(arbeidsledig)
        verify(saksbehandlingsService, times(++hit)).opprettOppgave(any())

        spreOppgaverService.soknadSendt(annet)
        verify(saksbehandlingsService, times(++hit)).opprettOppgave(any())

        spreOppgaverService.soknadSendt(behandlingsdagerSelvstendig)
        verify(saksbehandlingsService, times(++hit)).opprettOppgave(any())

        spreOppgaverService.soknadSendt(behandlingsdagerArbeidstaker)
        verify(saksbehandlingsService, times(++hit)).opprettOppgave(any())
    }

    @Test
    fun opprettOppgave() {
        val arbeidstaker = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)

        spreOppgaverService.soknadSendt(arbeidstaker)
        verify(saksbehandlingsService, times(1)).opprettOppgave(any())
    }


}

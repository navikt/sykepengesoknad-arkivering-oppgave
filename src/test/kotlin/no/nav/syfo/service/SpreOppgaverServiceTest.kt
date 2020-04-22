package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import no.nav.syfo.TestApplication
import no.nav.syfo.config.unleash.ToggleImpl
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.repository.SpreOppgave
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
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
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class SpreOppgaverServiceTest {
    @Mock
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @Mock
    lateinit var toggle: ToggleImpl

    lateinit var spreOppgaverService: SpreOppgaverService

    @Mock
    lateinit var oppgavestyringDAO: OppgavestyringDAO

    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), KotlinModule())
    private val sok = objectMapper.readValue(TestApplication::class.java.getResource("/soknadArbeidstakerMedNeisvar.json"), Sykepengesoknad::class.java)
    private val now = LocalDateTime.now()

    @Before
    fun setup() {
        spreOppgaverService = SpreOppgaverService("1", toggle, saksbehandlingsService, oppgavestyringDAO)
        whenever(toggle.isNotProduction()).thenReturn(false)
    }

    fun settTestMiljø(status: OppgaveStatus, timeout: LocalDateTime?) {
        whenever(toggle.isNotProduction()).thenReturn(true)
        whenever(oppgavestyringDAO.hentSpreOppgave(anyString())).thenAnswer {
            SpreOppgave(
                søknadsId = it.arguments[0].toString(),
                timeout = timeout,
                status = status,
                opprettet = LocalDateTime.now(),
                modifisert = LocalDateTime.now()
            )
        }
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

    @Test
    fun `status null og oppdatering utsett skal kalle nySpreOppgave med status utsett`() {
        whenever(toggle.isNotProduction()).thenReturn(true)
        val id = UUID.randomUUID()
        val timeout = LocalDateTime.now()
        val oppgave = OppgaveDTO(
            dokumentId = id,
            dokumentType = DokumentTypeDTO.Søknad,
            oppdateringstype = OppdateringstypeDTO.Utsett,
            timeout = timeout
        )
        spreOppgaverService.prosesserOppgave(oppgave)
        verify(oppgavestyringDAO, times(1)).nySpreOppgave(id.toString(), timeout, OppgaveStatus.Utsett)
    }

    @Test
    fun `status null og oppdatering opprett skal kalle nySpreOppgave med status opprett`() {
        whenever(toggle.isNotProduction()).thenReturn(true)
        val id = UUID.randomUUID()
        val oppgave = OppgaveDTO(
            dokumentId = id,
            dokumentType = DokumentTypeDTO.Søknad,
            oppdateringstype = OppdateringstypeDTO.Opprett,
            timeout = null
        )
        spreOppgaverService.prosesserOppgave(oppgave)
        verify(oppgavestyringDAO, times(1)).nySpreOppgave(id.toString(), null, OppgaveStatus.Opprett)
    }

    @Test
    fun `status null og oppdatering ferdigbehandlet skal kalle nySpreOppgave med status ikkeopprett`() {
        whenever(toggle.isNotProduction()).thenReturn(true)
        val id = UUID.randomUUID()
        val oppgave = OppgaveDTO(
            dokumentId = id,
            dokumentType = DokumentTypeDTO.Søknad,
            oppdateringstype = OppdateringstypeDTO.Ferdigbehandlet,
            timeout = null
        )
        spreOppgaverService.prosesserOppgave(oppgave)
        verify(oppgavestyringDAO, times(1)).nySpreOppgave(id.toString(), null, OppgaveStatus.IkkeOpprett)
    }

    @Test
    fun `status Utsett og oppdatering utsett skal kalle settTimeout med ny timeout`() {
        val timeout = LocalDateTime.now()
        settTestMiljø(OppgaveStatus.Utsett, timeout.minusHours(1))

        val id = UUID.randomUUID()
        val oppgave = OppgaveDTO(
            dokumentId = id,
            dokumentType = DokumentTypeDTO.Søknad,
            oppdateringstype = OppdateringstypeDTO.Utsett,
            timeout = timeout
        )
        spreOppgaverService.prosesserOppgave(oppgave)
        verify(oppgavestyringDAO, times(1)).settTimeout(id.toString(), timeout)
    }

    @Test
    fun `status Utsett og oppdatering utsett skal ikke kalle settTimeout når timeout er før eksisterende timeout`() {
        val timeout = LocalDateTime.now()
        settTestMiljø(OppgaveStatus.Utsett, timeout)

        val id = UUID.randomUUID()
        val oppgave = OppgaveDTO(
            dokumentId = id,
            dokumentType = DokumentTypeDTO.Søknad,
            oppdateringstype = OppdateringstypeDTO.Utsett,
            timeout = timeout.minusHours(1)
        )
        spreOppgaverService.prosesserOppgave(oppgave)
        verify(oppgavestyringDAO, never()).settTimeout(id.toString(), timeout)
    }

    @Test
    fun `status Utsett og oppdatering Opprett skal sette timeout til null, og status til Opprett`() {
        settTestMiljø(OppgaveStatus.Utsett, LocalDateTime.now())
        val id = UUID.randomUUID()
        val oppgave = OppgaveDTO(
            dokumentId = id,
            dokumentType = DokumentTypeDTO.Søknad,
            oppdateringstype = OppdateringstypeDTO.Opprett,
            timeout = null
        )

        spreOppgaverService.prosesserOppgave(oppgave)
        verify(oppgavestyringDAO, times(1)).settTimeout(id.toString(), null)
        verify(oppgavestyringDAO, times(1)).settStatus(id.toString(), OppgaveStatus.Opprett)
    }

    @Test
    fun `status Utsett og oppdatering Ferdigbehandlet skal sette timeout til null, og status til IkkeOpprett`() {
        settTestMiljø(OppgaveStatus.Utsett, LocalDateTime.now())
        val id = UUID.randomUUID()
        val oppgave = OppgaveDTO(
            dokumentId = id,
            dokumentType = DokumentTypeDTO.Søknad,
            oppdateringstype = OppdateringstypeDTO.Ferdigbehandlet,
            timeout = null
        )

        spreOppgaverService.prosesserOppgave(oppgave)
        verify(oppgavestyringDAO, times(1)).settTimeout(id.toString(), null)
        verify(oppgavestyringDAO, times(1)).settStatus(id.toString(), OppgaveStatus.IkkeOpprett)
    }

    @Test
    fun `status Opprett og oppdatering Utsett skal ikke kalle noen oppgavestyringsfunksjoner`() {
        settTestMiljø(OppgaveStatus.Opprett, LocalDateTime.now())
        val id = UUID.randomUUID()
        val oppgave = OppgaveDTO(
            dokumentId = id,
            dokumentType = DokumentTypeDTO.Søknad,
            oppdateringstype = OppdateringstypeDTO.Utsett,
            timeout = LocalDateTime.MAX
        )

        spreOppgaverService.prosesserOppgave(oppgave)
        verify(oppgavestyringDAO, never()).settTimeout(any(), any())
        verify(oppgavestyringDAO, never()).settStatus(any(), any())
        verify(oppgavestyringDAO, never()).nySpreOppgave(any(), any(), any())
    }
}

package no.nav.helse.flex.service

import com.nhaarman.mockitokotlin2.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import no.nav.helse.flex.client.SyfosoknadClient
import no.nav.helse.flex.client.SøknadIkkeFunnetException
import no.nav.helse.flex.config.EnvironmentToggles
import no.nav.helse.flex.repository.*
import no.nav.helse.flex.sykepengesoknad.kafka.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class OppgaveOpprettelseTest {
    @Mock
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @Mock
    lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @Mock
    lateinit var syfosoknadConsumer: SyfosoknadClient

    @Mock
    lateinit var environmentToggles: EnvironmentToggles

    @Mock
    lateinit var registry: MeterRegistry

    @Mock
    lateinit var identService: IdentService

    @InjectMocks
    lateinit var oppgaveOpprettelse: OppgaveOpprettelse

    private fun mockRegistry() {
        whenever(registry.counter(any(), any<Iterable<Tag>>())).thenReturn(mock())
    }

    private fun mockHenting() {
        whenever(syfosoknadConsumer.hentSoknad(any())).thenReturn(
            SykepengesoknadDTO(
                id = UUID.randomUUID().toString(),
                opprettet = LocalDateTime.now(),
                fom = LocalDate.of(2019, 5, 4),
                tom = LocalDate.of(2019, 5, 8),
                type = SoknadstypeDTO.ARBEIDSTAKERE,
                sporsmal = listOf(
                    SporsmalDTO(
                        id = UUID.randomUUID().toString(),
                        tag = "TAGGEN",
                        sporsmalstekst = "Fungerer rebehandlinga?",
                        svartype = SvartypeDTO.JA_NEI,
                        svar = listOf(SvarDTO(verdi = "JA"))

                    )
                ),
                status = SoknadsstatusDTO.SENDT,
                sendtNav = LocalDateTime.now(),
                fnr = "fnr"
            )
        )
        whenever(identService.hentAktorIdForFnr(any())).thenReturn(
            "aktor"
        )
    }

    @Test
    fun `har ingenting å behandle`() {
        oppgaveOpprettelse.behandleOppgaver()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
    }

    @Test
    fun `har noe å behandle men mangler innsending`() {
        mockRegistry()
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = UUID.randomUUID().toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = false
                )
            )
        )
        oppgaveOpprettelse.behandleOppgaver()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
    }

    @Test
    fun `sletter ikke oppgave(i test) om vi mangler innsending og den er fersk`() {
        mockRegistry()
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = UUID.randomUUID().toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = false
                )
            )
        )
        oppgaveOpprettelse.behandleOppgaver()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
        verify(spreOppgaveRepository, never()).deleteOppgaveBySykepengesoknadId(any())
    }

    @Test
    fun `sletter oppgave(i test) om vi mangler innsending og den er gammel`() {
        mockRegistry()
        whenever(environmentToggles.isQ()).thenReturn(true)
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = UUID.randomUUID().toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusDays(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = false
                )
            )
        )
        oppgaveOpprettelse.behandleOppgaver()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
        verify(spreOppgaveRepository, times(1)).deleteOppgaveBySykepengesoknadId(any())
    }

    @Test
    fun `har noe å behandle og har innsending`() {
        mockHenting()
        mockRegistry()
        val soknadId = UUID.randomUUID().toString()
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId,
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(soknadId)).thenReturn(
            InnsendingDbRecord(
                id = "iid",
                sykepengesoknadId = soknadId,
                journalpostId = "journalpost"
            )
        )

        val tidspunkt = Instant.now()
        oppgaveOpprettelse.behandleOppgaver(tidspunkt)

        verify(saksbehandlingsService, times(1)).opprettOppgave(any(), any(), any())
        verify(spreOppgaveRepository, times(1))
            .updateOppgaveBySykepengesoknadId(
                sykepengesoknadId = soknadId,
                timeout = null,
                status = OppgaveStatus.OpprettetTimeout,
                tidspunkt
            )
    }

    @Test
    fun `flere oppgaver hvor en tryner`() {
        mockRegistry()
        mockHenting()
        val soknadId1 = UUID.randomUUID()
        val soknadId2 = UUID.randomUUID()
        val soknadId3 = UUID.randomUUID()
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId1.toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true
                ),
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId2.toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true
                ),
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId3.toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(any())).thenAnswer {
            InnsendingDbRecord(
                id = "id",
                sykepengesoknadId = it.arguments[0].toString(),
                journalpostId = "journalpost"
            )
        }
        whenever(syfosoknadConsumer.hentSoknad(soknadId2.toString())).thenThrow(RuntimeException("I AM ERROR"))

        val tidspunkt = Instant.now()
        oppgaveOpprettelse.behandleOppgaver(tidspunkt)

        verify(saksbehandlingsService, times(2)).opprettOppgave(any(), any(), any())
        verify(spreOppgaveRepository, times(1))
            .updateOppgaveBySykepengesoknadId(
                sykepengesoknadId = soknadId1.toString(),
                timeout = null,
                status = OppgaveStatus.OpprettetTimeout,
                tidspunkt
            )
        verify(spreOppgaveRepository, times(1))
            .updateOppgaveBySykepengesoknadId(
                sykepengesoknadId = soknadId3.toString(),
                timeout = null,
                status = OppgaveStatus.OpprettetTimeout,
                tidspunkt
            )
    }

    @Test
    fun `Finner ikke søknad, skippes i Q`() {
        val soknadId = UUID.randomUUID()
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId.toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(soknadId.toString())).thenReturn(
            InnsendingDbRecord(
                id = "id",
                sykepengesoknadId = soknadId.toString(),
                journalpostId = "journalpost"
            )
        )
        whenever(environmentToggles.isQ()).thenReturn(true)
        whenever(syfosoknadConsumer.hentSoknad(soknadId.toString())).thenThrow(SøknadIkkeFunnetException("finner ikke"))
        val modifisertTidspunkt = Instant.now()
        oppgaveOpprettelse.behandleOppgaver(modifisertTidspunkt)

        verify(spreOppgaveRepository, times(1))
            .updateOppgaveBySykepengesoknadId(
                sykepengesoknadId = soknadId.toString(),
                timeout = null,
                status = OppgaveStatus.IkkeOpprett,
                modifisertTidspunkt
            )
    }

    @Test
    fun `Finner ikke søknad, skippes ikke i P`() {
        val soknadId = UUID.randomUUID()
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse()).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId.toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true
                )
            )
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(soknadId.toString())).thenReturn(
            InnsendingDbRecord(
                id = "id",
                sykepengesoknadId = soknadId.toString(),
                journalpostId = "journalpost"
            )
        )
        whenever(environmentToggles.isQ()).thenReturn(false)
        whenever(syfosoknadConsumer.hentSoknad(soknadId.toString())).thenThrow(SøknadIkkeFunnetException("msg"))
        assertThrows(SøknadIkkeFunnetException::class.java) {
            oppgaveOpprettelse.behandleOppgaver()
        }
        verify(spreOppgaveRepository, never()).updateOppgaveBySykepengesoknadId(any(), any(), any(), any())
    }
}

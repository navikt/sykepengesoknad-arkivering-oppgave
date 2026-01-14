package no.nav.helse.flex.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.helse.flex.client.SykepengesoknadBackendClient
import no.nav.helse.flex.client.SøknadIkkeFunnetException
import no.nav.helse.flex.config.EnvironmentToggles
import no.nav.helse.flex.repository.InnsendingDbRecord
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveDbRecord
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvartypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Duration
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
    lateinit var sykepengesoknadBackendClient: SykepengesoknadBackendClient

    @Mock
    lateinit var environmentToggles: EnvironmentToggles

    @Mock
    lateinit var identService: IdentService

    @InjectMocks
    lateinit var oppgaveOpprettelse: OppgaveOpprettelse

    private fun mockHentSoknad() {
        whenever(sykepengesoknadBackendClient.hentSoknad(any())).thenReturn(
            SykepengesoknadDTO(
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
                            sporsmalstekst = "Fungerer rebehandlinga?",
                            svartype = SvartypeDTO.JA_NEI,
                            svar = listOf(SvarDTO(verdi = "JA")),
                        ),
                    ),
                status = SoknadsstatusDTO.SENDT,
                sendtNav = LocalDateTime.now(),
                fnr = "fnr",
            ),
        )
        whenever(identService.hentAktorIdForFnr(any())).thenReturn(
            "aktor",
        )
    }

    @Test
    fun `Det er ingen oppgaver å behandle`() {
        oppgaveOpprettelse.behandleOppgaver()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
    }

    @Test
    fun `Oppgaver som kan behandles mangler innsending`() {
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse(any())).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = UUID.randomUUID().toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = false,
                ),
            ),
        )
        oppgaveOpprettelse.behandleOppgaver()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
    }

    @Test
    fun `Sletter ikke oppgave i Dev om vi mangler innsending og den er ny`() {
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse(any())).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = UUID.randomUUID().toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = false,
                ),
            ),
        )
        oppgaveOpprettelse.behandleOppgaver()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
        verify(spreOppgaveRepository, never()).deleteOppgaveBySykepengesoknadId(any())
    }

    @Test
    fun `Sletter oppgave i Dev om vi mangler innsending og den er gammel`() {
        whenever(environmentToggles.isDevGcp()).thenReturn(true)
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse(any())).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = UUID.randomUUID().toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusDays(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = false,
                ),
            ),
        )
        oppgaveOpprettelse.behandleOppgaver()
        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
        verify(spreOppgaveRepository, times(1)).deleteOppgaveBySykepengesoknadId(any())
    }

    @Test
    fun `Behandler oppgaver med innsending`() {
        mockHentSoknad()
        val soknadId = UUID.randomUUID().toString()
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse(any())).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId,
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true,
                ),
            ),
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(soknadId)).thenReturn(
            InnsendingDbRecord(
                id = "iid",
                sykepengesoknadId = soknadId,
                journalpostId = "journalpost",
            ),
        )

        val tidspunkt = Instant.now()
        oppgaveOpprettelse.behandleOppgaver(tidspunkt)

        verify(saksbehandlingsService, times(1)).opprettOppgave(any(), any(), any())
        verify(spreOppgaveRepository, times(1))
            .updateOppgaveBySykepengesoknadId(
                sykepengesoknadId = soknadId,
                timeout = null,
                status = OppgaveStatus.OpprettetTimeout,
                tidspunkt,
            )
    }

    @Test
    fun `Behandler resterende oppgaver selv om én feiler`() {
        mockHentSoknad()
        val soknadId1 = UUID.randomUUID()
        val soknadId2 = UUID.randomUUID()
        val soknadId3 = UUID.randomUUID()
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse(any())).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId1.toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true,
                ),
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId2.toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true,
                ),
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId3.toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true,
                ),
            ),
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(any())).thenAnswer {
            InnsendingDbRecord(
                id = "id",
                sykepengesoknadId = it.arguments[0].toString(),
                journalpostId = "journalpost",
            )
        }
        whenever(sykepengesoknadBackendClient.hentSoknad(soknadId2.toString())).thenThrow(RuntimeException("Error"))

        val tidspunkt = Instant.now()
        oppgaveOpprettelse.behandleOppgaver(tidspunkt)

        verify(saksbehandlingsService, times(2)).opprettOppgave(any(), any(), any())
        verify(spreOppgaveRepository, times(1))
            .updateOppgaveBySykepengesoknadId(
                sykepengesoknadId = soknadId1.toString(),
                timeout = null,
                status = OppgaveStatus.OpprettetTimeout,
                tidspunkt,
            )
        verify(spreOppgaveRepository, times(1))
            .updateOppgaveBySykepengesoknadId(
                sykepengesoknadId = soknadId3.toString(),
                timeout = null,
                status = OppgaveStatus.OpprettetTimeout,
                tidspunkt,
            )
    }

    @Test
    fun `Feiler ikke når mangler i Dev`() {
        val soknadId = UUID.randomUUID()
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse(any())).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId.toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true,
                ),
            ),
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(soknadId.toString())).thenReturn(
            InnsendingDbRecord(
                id = "id",
                sykepengesoknadId = soknadId.toString(),
                journalpostId = "journalpost",
            ),
        )
        whenever(environmentToggles.isDevGcp()).thenReturn(true)
        whenever(sykepengesoknadBackendClient.hentSoknad(soknadId.toString())).thenThrow(SøknadIkkeFunnetException("finner ikke"))
        val modifisertTidspunkt = Instant.now()
        oppgaveOpprettelse.behandleOppgaver(modifisertTidspunkt)

        verify(spreOppgaveRepository, times(1))
            .updateOppgaveBySykepengesoknadId(
                sykepengesoknadId = soknadId.toString(),
                timeout = null,
                status = OppgaveStatus.IkkeOpprett,
                modifisertTidspunkt,
            )
    }

    @Test
    fun `Feiler når søknad ikke finnes i Prod`() {
        val soknadId = UUID.randomUUID()
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse(any())).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId.toString(),
                    timeout = OffsetDateTime.now().minusHours(1).toInstant(),
                    status = OppgaveStatus.Utsett,
                    opprettet = OffsetDateTime.now().minusHours(2).toInstant(),
                    modifisert = OffsetDateTime.now().minusHours(1).toInstant(),
                    avstemt = true,
                ),
            ),
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(soknadId.toString())).thenReturn(
            InnsendingDbRecord(
                id = "id",
                sykepengesoknadId = soknadId.toString(),
                journalpostId = "journalpost",
            ),
        )
        whenever(environmentToggles.isDevGcp()).thenReturn(false)
        whenever(sykepengesoknadBackendClient.hentSoknad(soknadId.toString())).thenThrow(SøknadIkkeFunnetException("msg"))
        assertThrows(SøknadIkkeFunnetException::class.java) {
            oppgaveOpprettelse.behandleOppgaver()
        }
        verify(spreOppgaveRepository, never()).updateOppgaveBySykepengesoknadId(any(), any(), any(), any())
    }

    @Test
    fun `Utsetter Gosys-oppgave hvis søknad har medlemskapsspørsmål og det er mindre enn 10 minutter siden oppgaven ble opprettet`() {
        val soknadId = UUID.randomUUID().toString()
        val opprettetTid = Instant.now()
        whenever(sykepengesoknadBackendClient.hentSoknad(soknadId)).thenReturn(lagSoknad(soknadId))
        whenever(identService.hentAktorIdForFnr(any())).thenReturn("aktor")
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse(any())).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId,
                    status = OppgaveStatus.Opprett,
                    opprettet = opprettetTid,
                    avstemt = true,
                ),
            ),
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(soknadId)).thenReturn(
            InnsendingDbRecord(
                id = "id",
                sykepengesoknadId = soknadId,
                journalpostId = "journalpost",
            ),
        )

        oppgaveOpprettelse.behandleOppgaver(Instant.now())

        verify(saksbehandlingsService, never()).opprettOppgave(any(), any(), any())
    }

    @Test
    fun `Oppretter Gosys-oppgave hvis søknad har medlemskapsspørsmål og det mer enn 10 minutter siden oppgaven ble opprettet`() {
        val soknadId = UUID.randomUUID().toString()
        val opprettetTid = Instant.now().minus(Duration.ofMinutes(11))
        whenever(sykepengesoknadBackendClient.hentSoknad(soknadId)).thenReturn(lagSoknad(soknadId))
        whenever(identService.hentAktorIdForFnr(any())).thenReturn("aktor")
        whenever(spreOppgaveRepository.findOppgaverTilOpprettelse(any())).thenReturn(
            listOf(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = soknadId,
                    status = OppgaveStatus.Opprett,
                    opprettet = opprettetTid,
                    avstemt = true,
                ),
            ),
        )
        whenever(saksbehandlingsService.finnEksisterendeInnsending(soknadId)).thenReturn(
            InnsendingDbRecord(
                id = "id",
                sykepengesoknadId = soknadId,
                journalpostId = "journalpost",
            ),
        )

        val tid = Instant.now()
        oppgaveOpprettelse.behandleOppgaver(tid)

        verify(saksbehandlingsService, times(1)).opprettOppgave(any(), any(), any())
        verify(spreOppgaveRepository, times(1))
            .updateOppgaveBySykepengesoknadId(
                sykepengesoknadId = soknadId,
                timeout = null,
                status = OppgaveStatus.Opprettet,
                modifisert = tid,
            )
    }

    private fun lagSoknad(soknadId: String) =
        SykepengesoknadDTO(
            id = soknadId,
            opprettet = LocalDateTime.now().minusMinutes(20),
            fom = LocalDate.of(2025, 1, 1),
            tom = LocalDate.of(2025, 1, 20),
            type = SoknadstypeDTO.ARBEIDSTAKERE,
            sporsmal =
                listOf(
                    SporsmalDTO(
                        id = UUID.randomUUID().toString(),
                        tag = "MEDLEMSKAP_OPPHOLDSTILLATELSE",
                        svartype = SvartypeDTO.JA_NEI,
                        svar = emptyList(),
                    ),
                ),
            status = SoknadsstatusDTO.SENDT,
            sendtNav = LocalDateTime.now().minusMinutes(11),
            fnr = "fnr",
        )
}

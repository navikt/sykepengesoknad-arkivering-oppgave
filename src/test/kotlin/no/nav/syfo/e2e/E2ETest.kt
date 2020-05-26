package no.nav.syfo.e2e

import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import no.nav.syfo.TestApplication
import no.nav.syfo.any
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.Innsending
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.kafka.consumer.SoknadSendtListener
import no.nav.syfo.kafka.consumer.SpreOppgaverListener
import no.nav.syfo.kafka.felles.SoknadsstatusDTO
import no.nav.syfo.kafka.felles.SoknadstypeDTO
import no.nav.syfo.kafka.felles.SporsmalDTO
import no.nav.syfo.kafka.felles.SvarDTO
import no.nav.syfo.kafka.felles.SvartypeDTO
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.service.BehandleVedTimeoutService
import no.nav.syfo.service.SaksbehandlingsService
import no.nav.syfo.skapConsumerRecord
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@RunWith(SpringRunner::class)
@EmbeddedKafka
@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
class E2ETest {

    companion object {
        val aktørId = "aktørId"
        val omFireTimer = LocalDateTime.now().plusHours(4)
    }

    @MockBean
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @MockBean
    lateinit var acknowledgment: Acknowledgment

    @MockBean
    lateinit var syfosoknadConsumer: SyfosoknadConsumer

    @Inject
    lateinit var spreOppgaverListener: SpreOppgaverListener

    @Inject
    lateinit var soknadSendtListener: SoknadSendtListener

    @Inject
    lateinit var spreOppgavestyringDAO: OppgavestyringDAO

    @Inject
    lateinit var behandleVedTimeoutService: BehandleVedTimeoutService

    @Before
    fun setup() {
        whenever(saksbehandlingsService.finnEksisterendeInnsending(any())).thenAnswer {
            Innsending(
                innsendingsId = "iid",
                ressursId = it.arguments[0].toString(),
                aktorId = aktørId,
                saksId = "saksId",
                journalpostId = "journalpost"
            )
        }
        whenever(syfosoknadConsumer.hentSoknad(any())).thenReturn(søknad())
    }

    @Test
    fun `bømlo sier opprett før vi behandler søknad`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, søknadsId, null))

        val oppgave = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Opprett, oppgave.status)
        assertNull(oppgave.timeout)
        assertFalse(oppgave.avstemt)
    }

    @Test
    fun `bømlo sier utsett før vi behandler søknad`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))

        val oppgave = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Utsett, oppgave.status)
        assertEquals(omFireTimer, oppgave.timeout)
        assertFalse(oppgave.avstemt)
    }

    @Test
    fun `bømlo sier ferdigbehandlet før vi behandler søknad`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Ferdigbehandlet, søknadsId, null))

        val oppgave = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.IkkeOpprett, oppgave.status)
        assertNull(oppgave.timeout)
        assertFalse(oppgave.avstemt)
    }

    @Test
    fun `vi behandler søknad så kommer utsett fra bømlo`() {
        val søknadsId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(søknadsId))
        leggOppgavePåKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))

        val oppgave = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Utsett, oppgave.status)
        assertEquals(omFireTimer, oppgave.timeout)
        assertTrue(oppgave.avstemt)
    }

    @Test
    fun `vi behandler søknad så kommer opprett fra bømlo`() {
        val søknadsId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(søknadsId))
        leggOppgavePåKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, søknadsId))

        val oppgave = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Opprett, oppgave.status)
        assertNull(oppgave.timeout)
        assertTrue(oppgave.avstemt)
    }

    @Test
    fun `vi behandler søknad så kommer ferdigbehandlet fra bømlo`() {
        val søknadsId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(søknadsId))
        leggOppgavePåKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Ferdigbehandlet, søknadsId))

        val oppgave = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.IkkeOpprett, oppgave.status)
        assertNull(oppgave.timeout)
        assertTrue(oppgave.avstemt)
    }

    @Test
    fun `bømlo sier utsett så behandler vi søknaden og utsetter oppgave`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))
        leggSøknadPåKafka(søknad(søknadsId))

        val oppgave = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Utsett, oppgave.status)
        assertEquals(omFireTimer, oppgave.timeout)
        assertTrue(oppgave.avstemt)
    }

    @Test
    fun `bømlo sier utsett etter oppgaven er opprettet`() {
        val søknadsId = UUID.randomUUID()
        val førsteTimeout = LocalDateTime.now().minusHours(1)
        leggOppgavePåKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, førsteTimeout))
        leggSøknadPåKafka(søknad(søknadsId))

        behandleVedTimeoutService.behandleTimeout()
        leggOppgavePåKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))

        val oppgave = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Opprettet, oppgave.status)
        assertNull(oppgave.timeout)
    }

    @Test
    fun `bømlo sier utsett etter oppgaven er ferdigbehandlet`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Ferdigbehandlet, søknadsId))
        leggSøknadPåKafka(søknad(søknadsId))
        leggOppgavePåKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))

        val oppgave = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.IkkeOpprett, oppgave.status)
        assertNull(oppgave.timeout)
    }

    @Test
    fun `bømlo sier utsett så behandler vi søknaden og ikke oppretter oppgave`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåKafka(
            OppgaveDTO(
                dokumentType = DokumentTypeDTO.Søknad,
                oppdateringstype = OppdateringstypeDTO.Utsett,
                dokumentId = søknadsId,
                timeout = LocalDateTime.now().plusHours(1)
            )
        )
        leggSøknadPåKafka(søknad(søknadsId = søknadsId, sendtNav = null, sendtArbeidsgiver = LocalDateTime.now()))

        val oppgaveFørJob = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Utsett, oppgaveFørJob.status)
        assertFalse(oppgaveFørJob.avstemt)

        behandleVedTimeoutService.behandleTimeout()

        val oppgaveEtterJob = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Utsett, oppgaveEtterJob.status)
        assertFalse(oppgaveEtterJob.avstemt)
    }

    @Test
    fun `vi behandler søknaden uten oppgave så sender bømlo utsett`() {
        val søknadsId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(søknadsId = søknadsId, sendtNav = null, sendtArbeidsgiver = LocalDateTime.now()))
        leggOppgavePåKafka(
            OppgaveDTO(
                dokumentType = DokumentTypeDTO.Søknad,
                oppdateringstype = OppdateringstypeDTO.Utsett,
                dokumentId = søknadsId,
                timeout = omFireTimer
            )
        )

        val oppgave = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Utsett, oppgave.status)
        assertEquals(omFireTimer, oppgave.timeout)
        assertFalse(oppgave.avstemt)
    }

    private fun leggOppgavePåKafka(oppgave: OppgaveDTO) =
        spreOppgaverListener.listen(skapConsumerRecord("key", oppgave), acknowledgment)

    private fun leggSøknadPåKafka(søknad: SykepengesoknadDTO) =
        soknadSendtListener.listen(skapConsumerRecord("key", søknad), acknowledgment)

    private fun søknad(
        søknadsId: UUID = UUID.randomUUID(),
        sendtNav: LocalDateTime? = LocalDateTime.now(),
        sendtArbeidsgiver: LocalDateTime? = null
    ) = SykepengesoknadDTO(
        aktorId = aktørId,
        id = søknadsId.toString(),
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
        sendtNav = sendtNav,
        sendtArbeidsgiver = sendtArbeidsgiver,
        fodselsnummer = null
    )
}
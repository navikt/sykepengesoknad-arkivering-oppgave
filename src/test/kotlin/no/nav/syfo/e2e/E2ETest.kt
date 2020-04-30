package no.nav.syfo.e2e

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import no.nav.syfo.TestApplication
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.domain.DokumentTypeDTO
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
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
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
    }

    @Inject
    lateinit var spreOppgaverListener: SpreOppgaverListener

    @Inject
    lateinit var soknadSendtListener: SoknadSendtListener

    @Inject
    lateinit var spreOppgavestyringDAO: OppgavestyringDAO

    @Inject
    lateinit var behandleVedTimeoutService: BehandleVedTimeoutService

    @MockBean
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @Mock
    lateinit var acknowledgment: Acknowledgment
    /*
 #1: Vi sender utsett før flex har journalført
 #2: Vi sender opprett før flex har journalført
 #3: Vi sender forkast før flex har journalført

 #4: Vi sender utsett etter flex har journalført
 #5: Vi sender opprett etter flex har journalført
 #6: Vi sender forkast etter flex har journalført

 #7: Vi sender utsett etter flex har opprettet oppgave
 #8: Vi sender opprett etter flex har opprettet oppgave
 #9: Vi sender forkast etter flex har opprettet oppgave


 #11 Vi sender opprett på søknad som flex ikke mener skal ha oppgave
 #12 Vi sender forkast på søknad som flex ikke mener skal ha oppgave
     */

    @Test
    fun `bømlo sier utsett så behandler vi søknaden og utsetter oppgave`() {
        val timeout = LocalDateTime.now().plusHours(4)
        val søknadsId = UUID.randomUUID()
        spreOppgaverListener.listen(
            skapConsumerRecord(
                "key", OppgaveDTO(
                    DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, timeout
                )
            ), acknowledgment)
        soknadSendtListener.listen(skapConsumerRecord("key", søknad(søknadsId)), acknowledgment)

        val oppgave = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Utsett, oppgave.status)
        assertEquals(timeout, oppgave.timeout)
        assertTrue(oppgave.avstemt)
    }

    @Test
    fun `bømlo sier utsett så behandler vi søknaden og ikke oppretter oppgave`() {
        val søknadsId = UUID.randomUUID()
        spreOppgaverListener.listen(
            skapConsumerRecord(
                "key", OppgaveDTO(
                    DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, LocalDateTime.now().plusHours(1)
                )
            ), acknowledgment)
        soknadSendtListener.listen(skapConsumerRecord("key", søknad(
            søknadsId = søknadsId,
            sendtNav = null,
            sendtArbeidsgiver = LocalDateTime.now()
        )), acknowledgment)

        val oppgaveFørJob = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Utsett, oppgaveFørJob.status)
        assertFalse(oppgaveFørJob.avstemt)

        behandleVedTimeoutService.behandleTimeout()

        val oppgaveEtterJob = requireNotNull(spreOppgavestyringDAO.hentSpreOppgave(søknadsId.toString()))
        assertEquals(OppgaveStatus.Utsett, oppgaveEtterJob.status)
        assertFalse(oppgaveEtterJob.avstemt)
    }

    private fun søknad(søknadsId: UUID = UUID.randomUUID(), sendtNav: LocalDateTime? = LocalDateTime.now(), sendtArbeidsgiver: LocalDateTime? = null) = SykepengesoknadDTO(
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
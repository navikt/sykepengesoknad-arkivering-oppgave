package no.nav.syfo.kafka.consumer

import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import no.nav.syfo.TestApplication
import no.nav.syfo.config.unleash.ToggleImpl
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.skapConsumerRecord
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.reset
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@RunWith(SpringRunner::class)
@EmbeddedKafka
@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
class SpreOppgaverListenerTest {
    @Mock
    private lateinit var acknowledgment: Acknowledgment

    @Inject
    lateinit var spreOppgaverListener: SpreOppgaverListener

    @Inject
    lateinit var oppgavestyringDAO: OppgavestyringDAO

    @Inject
    lateinit var toggle: ToggleImpl

    @Before
    fun setup() {
        reset(toggle)
        whenever(toggle.isNotProduction()).thenReturn(true)
    }

    @Test
    fun `bømlo sier opprett før vi har lagret oppgave`() {
        val id = UUID.randomUUID()
        val oppgave1 = OppgaveDTO(
            dokumentType = DokumentTypeDTO.Søknad,
            dokumentId = id,
            oppdateringstype = OppdateringstypeDTO.Opprett,
            timeout = null
        )

        spreOppgaverListener.listen(skapConsumerRecord("1", oppgave1), acknowledgment)
        val oppgaveIDb = requireNotNull(oppgavestyringDAO.hentSpreOppgave(id.toString()))
        assertEquals(OppgaveStatus.Opprett, oppgaveIDb.status)
        assertEquals(id.toString(), oppgaveIDb.søknadsId)
    }

    @Test
    fun `bømlo sier utsett før vi har lagret oppgave`() {
        val id = UUID.randomUUID()
        val timeout = LocalDateTime.now()
        val oppgave1 = OppgaveDTO(
            dokumentType = DokumentTypeDTO.Søknad,
            dokumentId = id,
            oppdateringstype = OppdateringstypeDTO.Utsett,
            timeout = timeout
        )

        spreOppgaverListener.listen(skapConsumerRecord("1", oppgave1), acknowledgment)
        val oppgaveIDb = requireNotNull(oppgavestyringDAO.hentSpreOppgave(id.toString()))
        assertEquals(OppgaveStatus.Utsett, oppgaveIDb.status)
        assertEquals(id.toString(), oppgaveIDb.søknadsId)
        assertEquals(timeout, oppgaveIDb.timeout)
    }

    @Test
    fun `bømlo sier ferdigbehandlet før vi har lagret oppgave`() {
        val id = UUID.randomUUID()
        val oppgave1 = OppgaveDTO(
            dokumentType = DokumentTypeDTO.Søknad,
            dokumentId = id,
            oppdateringstype = OppdateringstypeDTO.Ferdigbehandlet,
            timeout = null
        )

        spreOppgaverListener.listen(skapConsumerRecord("1", oppgave1), acknowledgment)
        val oppgaveIDb = requireNotNull(oppgavestyringDAO.hentSpreOppgave(id.toString()))
        assertEquals(OppgaveStatus.IkkeOpprett, oppgaveIDb.status)
        assertEquals(id.toString(), oppgaveIDb.søknadsId)
    }

    @Test
    fun `bømlo sier opprett etter vi har lagret oppgave`() {
        val id = UUID.randomUUID()

        oppgavestyringDAO.nySpreOppgave(id.toString(), LocalDateTime.now().plusHours(24), OppgaveStatus.Utsett)

        val oppgave1 = OppgaveDTO(
            dokumentType = DokumentTypeDTO.Søknad,
            dokumentId = id,
            oppdateringstype = OppdateringstypeDTO.Opprett,
            timeout = null
        )

        spreOppgaverListener.listen(skapConsumerRecord("1", oppgave1), acknowledgment)
        val oppgaveIDb = requireNotNull(oppgavestyringDAO.hentSpreOppgave(id.toString()))
        assertEquals(OppgaveStatus.Opprett, oppgaveIDb.status)
        assertEquals(id.toString(), oppgaveIDb.søknadsId)
        assertEquals(null, oppgaveIDb.timeout)
    }

    @Test
    fun `bømlo sier utsett etter vi har lagret oppgave`() {
        val id = UUID.randomUUID()

        oppgavestyringDAO.nySpreOppgave(id.toString(), LocalDateTime.now().plusHours(24), OppgaveStatus.Utsett)

        val timeout = LocalDateTime.now().plusHours(48)
        val oppgave1 = OppgaveDTO(
            dokumentType = DokumentTypeDTO.Søknad,
            dokumentId = id,
            oppdateringstype = OppdateringstypeDTO.Utsett,
            timeout = timeout
        )

        spreOppgaverListener.listen(skapConsumerRecord("1", oppgave1), acknowledgment)
        val oppgaveIDb = requireNotNull(oppgavestyringDAO.hentSpreOppgave(id.toString()))
        assertEquals(OppgaveStatus.Utsett, oppgaveIDb.status)
        assertEquals(id.toString(), oppgaveIDb.søknadsId)
        assertEquals(timeout, oppgaveIDb.timeout)
    }

    @Test
    fun `bømlo sier ferdigbehandlet etter vi har lagret oppgave`() {
        val id = UUID.randomUUID()

        oppgavestyringDAO.nySpreOppgave(id.toString(), LocalDateTime.now().plusHours(24), OppgaveStatus.Utsett)

        val oppgave1 = OppgaveDTO(
            dokumentType = DokumentTypeDTO.Søknad,
            dokumentId = id,
            oppdateringstype = OppdateringstypeDTO.Ferdigbehandlet,
            timeout = null
        )

        spreOppgaverListener.listen(skapConsumerRecord("1", oppgave1), acknowledgment)
        val oppgaveIDb = requireNotNull(oppgavestyringDAO.hentSpreOppgave(id.toString()))
        assertEquals(OppgaveStatus.IkkeOpprett, oppgaveIDb.status)
        assertEquals(id.toString(), oppgaveIDb.søknadsId)
        assertEquals(null, oppgaveIDb.timeout)
    }
}

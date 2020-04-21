package no.nav.syfo.kafka.consumer

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import no.nav.syfo.TestApplication
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.repository.OppgavestyringLogDAO
import no.nav.syfo.consumer.repository.SpreOppgave
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.skapConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.runner.RunWith

import org.junit.Test
import org.mockito.*
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
    lateinit var oppgavestyringLogDAO: OppgavestyringLogDAO

    @Inject
    lateinit var oppgavestyringDAO: OppgavestyringDAO

    @Test
    fun `oppgave blir opprettet på utsett og slettet på ferdigbehandlet`() {
        val id = UUID.randomUUID()
        val utsattOppgave = OppgaveDTO(
            dokumentType = DokumentTypeDTO.Søknad,
            dokumentId = id,
            oppdateringstype = OppdateringstypeDTO.Utsett,
            timeout = LocalDateTime.MAX
        )
        val ferdigbehandletOppgave = OppgaveDTO(
            dokumentType = DokumentTypeDTO.Søknad,
            dokumentId = id,
            oppdateringstype = OppdateringstypeDTO.Ferdigbehandlet,
            timeout = null
        )
        spreOppgaverListener.listen(skapConsumerRecord("1", utsattOppgave), acknowledgment)
        val oppgaveIDb = oppgavestyringDAO.hentSpreOppgave(id.toString())
        assertThat(oppgaveIDb).isEqualTo(SpreOppgave(søknadsId = id.toString(), timeout = LocalDateTime.MAX))

        spreOppgaverListener.listen(skapConsumerRecord("2", ferdigbehandletOppgave), acknowledgment)
        val ingenOppgave = oppgavestyringDAO.hentSpreOppgave(id.toString())
        assertThat(ingenOppgave).isNull()

        verify(acknowledgment, times(2)).acknowledge()

        val loggOppgaver = oppgavestyringLogDAO.hentEventerForSykepengesoknadId(id.toString())
        assertThat(loggOppgaver).hasSize(2)
        assertThat(loggOppgaver[0]).isEqualTo(utsattOppgave)
        assertThat(loggOppgaver[1]).isEqualTo(ferdigbehandletOppgave)
    }
}

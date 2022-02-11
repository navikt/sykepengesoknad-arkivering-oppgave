package no.nav.syfo.e2e

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.syfo.AbstractContainerBaseTest
import no.nav.syfo.TestApplication
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.kafka.consumer.AivenSpreOppgaverListener
import no.nav.syfo.repository.OppgaveRepository
import no.nav.syfo.repository.OppgaveStatus
import no.nav.syfo.repository.OppgavestyringDAO
import no.nav.syfo.serialisertTilString
import no.nav.syfo.skapConsumerRecord
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(classes = [TestApplication::class])
@DirtiesContext
@EnableMockOAuth2Server
class JulegateTest : AbstractContainerBaseTest() {

    companion object {
        val fnr = "fnr"
    }

    @MockBean
    lateinit var acknowledgment: Acknowledgment

    @Autowired
    lateinit var aivenSpreOppgaverListener: AivenSpreOppgaverListener

    @Autowired
    lateinit var oppgavestyringDAO: OppgavestyringDAO

    @Autowired
    lateinit var oppgaveRepository: OppgaveRepository

    @Test
    fun `Utsett til Ferdig til Opprett`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId))
        oppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.Utsett
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Ferdigbehandlet, søknadsId))
        oppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.IkkeOpprett
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, søknadsId))
        oppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.Opprett
    }

    @Test
    fun `Oppretter ikke en som er allerede opprettet`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId))
        oppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.Utsett
        oppgaveRepository.updateOppgaveBySykepengesoknadId(
            sykepengesoknadId = søknadsId.toString(),
            timeout = LocalDateTime.now(),
            status = OppgaveStatus.Opprettet
        )
        oppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.Opprettet

        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, søknadsId))

        // Fortsatt opprettet
        oppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.Opprettet
    }

    private fun leggOppgavePåAivenKafka(oppgave: OppgaveDTO) =
        aivenSpreOppgaverListener.listen(skapConsumerRecord("key", oppgave.serialisertTilString()), acknowledgment)
}

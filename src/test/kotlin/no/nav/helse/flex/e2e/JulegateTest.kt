package no.nav.helse.flex.e2e

import no.nav.helse.FellesTestoppsett
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.kafka.consumer.AivenSpreOppgaverListener
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.skapConsumerRecord
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant
import java.util.UUID

@DirtiesContext
class JulegateTest : FellesTestoppsett() {

    companion object {
        val fnr = "fnr"
    }

    @MockBean
    lateinit var acknowledgment: Acknowledgment

    @Autowired
    lateinit var aivenSpreOppgaverListener: AivenSpreOppgaverListener

    @Autowired
    lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @Test
    fun `Utsett til Ferdig til Opprett`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId))
        spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.Utsett
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Ferdigbehandlet, søknadsId))
        spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.IkkeOpprett
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, søknadsId))
        spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.Opprett
    }

    @Test
    fun `Oppretter ikke en som er allerede opprettet`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId))
        spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.Utsett
        spreOppgaveRepository.updateOppgaveBySykepengesoknadId(
            sykepengesoknadId = søknadsId.toString(),
            timeout = Instant.now(),
            status = OppgaveStatus.Opprettet
        )
        spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.Opprettet

        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, søknadsId))

        // Fortsatt opprettet
        spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString())!!.status shouldBeEqualTo OppgaveStatus.Opprettet
    }

    private fun leggOppgavePåAivenKafka(oppgave: OppgaveDTO) =
        aivenSpreOppgaverListener.listen(skapConsumerRecord("key", oppgave.serialisertTilString()), acknowledgment)
}

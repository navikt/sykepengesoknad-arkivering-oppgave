package no.nav.helse.flex.spreoppgave

import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveDbRecord
import no.nav.helse.flex.repository.SpreOppgaveRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
@Primary
class HandterOppgaveInterceptor(
    private val handterOppave: HandterOppave,
    private val spreOppgaveRepository: SpreOppgaveRepository,
) : HandterOppgaveInterface {
    companion object {
        val raceConditionUUID: UUID = UUID.randomUUID()
        val raceConditionTimeout: Instant = Instant.now().plusSeconds(60)
    }

    override fun håndterOppgaveFraBømlo(eksisterendeOppgave: SpreOppgaveDbRecord?, oppgave: OppgaveDTO) {
        if (eksisterendeOppgave == null && oppgave.dokumentId == raceConditionUUID) {
            insertSpreOppgaveMellomHentingAvEksisterendeOgLagring()
        }

        handterOppave.håndterOppgaveFraBømlo(eksisterendeOppgave, oppgave)
    }

    override fun håndterOppgaveFraSøknad(eksisterendeOppgave: SpreOppgaveDbRecord?, oppgave: OppgaveDTO) {
        if (eksisterendeOppgave == null && oppgave.dokumentId == raceConditionUUID) {
            insertSpreOppgaveMellomHentingAvEksisterendeOgLagring()
        }

        handterOppave.håndterOppgaveFraSøknad(eksisterendeOppgave, oppgave)
    }

    private fun insertSpreOppgaveMellomHentingAvEksisterendeOgLagring() {
        val tidspunkt = Instant.now()

        spreOppgaveRepository.save(
            SpreOppgaveDbRecord(
                sykepengesoknadId = raceConditionUUID.toString(),
                timeout = raceConditionTimeout,
                status = OppgaveStatus.Utsett,
                opprettet = tidspunkt,
                modifisert = tidspunkt
            )
        )
    }
}

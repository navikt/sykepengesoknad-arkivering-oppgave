package no.nav.helse.flex.spreoppgave

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.logger
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveDbRecord
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.util.tilOsloZone
import org.springframework.stereotype.Component
import java.time.Instant

interface HandterOppgaveInterface {
    fun håndterOppgaveFraBømlo(
        eksisterendeOppgave: SpreOppgaveDbRecord?,
        oppgave: OppgaveDTO
    )

    fun håndterOppgaveFraSøknad(
        eksisterendeOppgave: SpreOppgaveDbRecord?,
        oppgave: OppgaveDTO
    )
}

@Component
class HandterOppave(
    private val spreOppgaveRepository: SpreOppgaveRepository,
    registry: MeterRegistry
) : HandterOppgaveInterface {

    private val log = logger()
    private val gjenopplivetCounter = registry.counter("gjenopplivet_oppgave")

    override fun håndterOppgaveFraBømlo(
        eksisterendeOppgave: SpreOppgaveDbRecord?,
        oppgave: OppgaveDTO
    ) {
        val tidspunkt = Instant.now()
        when {
            eksisterendeOppgave == null -> {
                spreOppgaveRepository.save(
                    SpreOppgaveDbRecord(
                        sykepengesoknadId = oppgave.dokumentId.toString(),
                        timeout = timeout(oppgave),
                        status = oppgave.oppdateringstype.tilOppgaveStatus(),
                        opprettet = tidspunkt,
                        modifisert = tidspunkt
                    )
                )
            }
            eksisterendeOppgave.status == OppgaveStatus.Utsett -> {
                spreOppgaveRepository.updateOppgaveBySykepengesoknadId(
                    sykepengesoknadId = oppgave.dokumentId.toString(),
                    timeout = timeout(oppgave),
                    status = oppgave.oppdateringstype.tilOppgaveStatus(),
                    tidspunkt
                )
            }
            eksisterendeOppgave.status == OppgaveStatus.IkkeOpprett && oppgave.oppdateringstype == OppdateringstypeDTO.Opprett -> {
                log.info("Vil opprette oppgave for søknad ${oppgave.dokumentId} som vi tidligere ble bedt om å ikke opprette")
                gjenopplivetCounter.increment()
                spreOppgaveRepository.updateOppgaveBySykepengesoknadId(
                    sykepengesoknadId = oppgave.dokumentId.toString(),
                    timeout = timeout(oppgave),
                    status = oppgave.oppdateringstype.tilOppgaveStatus(),
                    tidspunkt
                )
            }
            else -> {
                log.info("Gjør ikke ${oppgave.oppdateringstype.name} for søknad ${oppgave.dokumentId} fordi status er ${eksisterendeOppgave.status.name}")
            }
        }
    }

    override fun håndterOppgaveFraSøknad(
        eksisterendeOppgave: SpreOppgaveDbRecord?,
        oppgave: OppgaveDTO
    ) {
        if (eksisterendeOppgave != null) {
            spreOppgaveRepository.updateAvstemtBySykepengesoknadId(eksisterendeOppgave.sykepengesoknadId)
        } else {
            val now = Instant.now()
            spreOppgaveRepository.save(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = oppgave.dokumentId.toString(),
                    timeout = timeout(oppgave),
                    status = oppgave.oppdateringstype.tilOppgaveStatus(),
                    avstemt = true,
                    opprettet = now,
                    modifisert = now
                )
            )
        }
    }
}

internal fun timeout(oppgave: OppgaveDTO) =
    if (oppgave.oppdateringstype == OppdateringstypeDTO.Utsett) {
        oppgave.timeout?.tilOsloZone()
            ?.toInstant()
    } else {
        null
    }

private fun OppdateringstypeDTO.tilOppgaveStatus() = when (this) {
    OppdateringstypeDTO.Utsett -> OppgaveStatus.Utsett
    OppdateringstypeDTO.Ferdigbehandlet -> OppgaveStatus.IkkeOpprett
    OppdateringstypeDTO.OpprettSpeilRelatert -> OppgaveStatus.OpprettSpeilRelatert
    OppdateringstypeDTO.Opprett -> OppgaveStatus.Opprett
}

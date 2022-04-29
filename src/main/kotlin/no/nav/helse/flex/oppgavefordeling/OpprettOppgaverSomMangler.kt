package no.nav.helse.flex.oppgavefordeling

import no.nav.helse.flex.client.SyfosoknadClient
import no.nav.helse.flex.kafka.mapper.toSykepengesoknad
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveDbRecord
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.service.IdentService
import no.nav.helse.flex.service.SaksbehandlingsService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class OpprettOppgaverSomMangler(
    private val oppgavefordelingRepository: OppgavefordelingRepository,
    private val innsendingRepository: InnsendingRepository,
    private val saksbehandlingsService: SaksbehandlingsService,
    private val syfosoknadClient: SyfosoknadClient,
    private val identService: IdentService,
    private val spreOppgaveRepository: SpreOppgaveRepository,
) {

    @Scheduled(initialDelay = 120, fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    fun startCronJob() {
        finnUferdigeOppgavefordelinger()
    }

    fun finnUferdigeOppgavefordelinger() {
        oppgavefordelingRepository
            .finnOppgaverSomMangler()
            .forEach { opprettOppgave(it) }
    }

    @Transactional
    fun opprettOppgave(
        oppgavefordeling: OppgavefordelingDbRecord
    ) {
        val innsending = innsendingRepository.findBySykepengesoknadId(oppgavefordeling.sykepengesoknadId)!!

        val soknadDTO = syfosoknadClient.hentSoknad(oppgavefordeling.sykepengesoknadId)
        val aktorId = identService.hentAktorIdForFnr(soknadDTO.fnr)
        val soknad = soknadDTO.toSykepengesoknad(aktorId)

        saksbehandlingsService.opprettOppgave(
            sykepengesoknad = soknad,
            innsending = innsending,
            speilRelatert = oppgavefordeling.status == OppgavefordelingStatus.LagOppgaveForSpeilsaksbehandlere,
            oppgavefordelingRelatert = true,
        )

        oppdaterSpreOppgave(oppgavefordeling)

        oppgavefordelingRepository.settTilFerdig(soknad.id)
    }

    fun oppdaterSpreOppgave(oppgavefordeling: OppgavefordelingDbRecord) {
        val spreOppgave = spreOppgaveRepository.findBySykepengesoknadId(oppgavefordeling.sykepengesoknadId)
        val now = Instant.now()

        if (spreOppgave == null) {
            spreOppgaveRepository.save(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = oppgavefordeling.sykepengesoknadId,
                    status = oppgavefordeling.status.tilOpprettetStatus(),
                    opprettet = now,
                    modifisert = now,
                    avstemt = true,
                )
            )
            return
        }

        if (spreOppgave.status in listOf(
                OppgaveStatus.Opprettet,
                OppgaveStatus.OpprettetSpeilRelatert,
                OppgaveStatus.OpprettetTimeout
            )
        ) {
            throw RuntimeException("Kan ikke opprette oppgave fordi spre oppgave har status ${spreOppgave.status}")
        }

        spreOppgaveRepository.updateOppgaveBySykepengesoknadId(
            sykepengesoknadId = oppgavefordeling.sykepengesoknadId,
            timeout = null,
            status = oppgavefordeling.status.tilOpprettetStatus(),
            now
        )
    }

    fun OppgavefordelingStatus.tilOpprettetStatus(): OppgaveStatus {
        return when (this) {
            OppgavefordelingStatus.LagOppgave -> OppgaveStatus.Opprettet
            OppgavefordelingStatus.LagOppgaveForSpeilsaksbehandlere -> OppgaveStatus.OpprettetSpeilRelatert
        }
    }
}

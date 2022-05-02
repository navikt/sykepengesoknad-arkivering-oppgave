package no.nav.helse.flex.oppgavefordeling

import no.nav.helse.flex.client.pdl.PdlClient
import no.nav.helse.flex.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class GeografiskTilknytning(
    private val oppgavefordelingRepository: OppgavefordelingRepository,
    private val pdlClient: PdlClient,
) {

    val log = logger()

    @Scheduled(initialDelay = 120, fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    fun fyllMedGeografiskTilknytning() {
        oppgavefordelingRepository.hent100UtenGT().forEach {
            try {
                val gt = pdlClient.hentGeografiskTilknytning(it.fnr!!).hentGeografiskTilknytning

                oppgavefordelingRepository.lagreGeografiskTilknytning(
                    sykepengesoknadId = it.sykepengesoknadId,
                    kommune = gt.gtKommune,
                    bydel = gt.gtBydel,
                    land = gt.gtLand,
                )
            } catch (e: Exception) {
                log.warn("Klarte ikke hent GT for sykepengesoknad ${it.sykepengesoknadId} - ${e.message}")
            }
        }
    }
}

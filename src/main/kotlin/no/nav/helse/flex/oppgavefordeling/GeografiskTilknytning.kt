package no.nav.helse.flex.oppgavefordeling

import no.nav.helse.flex.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class GeografiskTilknytning(private val oppgavefordelingRepository: OppgavefordelingRepository) {

    private val log = logger()

    @Scheduled(initialDelay = 120, fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    fun fyllMedGeografiskTilknytning() {
        TODO("Not yet implemented")
    }
}

package no.nav.syfo.service

import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.logger
import no.nav.syfo.repository.InnsendingDbRecord
import org.springframework.stereotype.Component

@Component
class BehandleFeiledeSoknaderService(private val spreOppgaverService: SpreOppgaverService) {
    private val log = logger()

    fun behandleFeiletSoknad(innsending: InnsendingDbRecord?, sykepengesoknad: Sykepengesoknad) {
        try {
            if (innsending?.behandlet != null) {
                log.warn(
                    "Forsøkte å rebehandle ferdigbehandlet søknad med innsendingid: {} og søknadsid: {}",
                    innsending.id, sykepengesoknad.id
                )
            } else {
                spreOppgaverService.soknadSendt(sykepengesoknad)
            }
        } catch (e: RuntimeException) {
            throw RuntimeException("Feilet ved rebehandling av innsending med sykepengesoknadId: ${sykepengesoknad.id}", e)
        }
    }
}

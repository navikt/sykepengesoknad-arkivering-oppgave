package no.nav.helse.service

import no.nav.helse.domain.dto.Sykepengesoknad
import no.nav.helse.logger
import no.nav.helse.repository.InnsendingDbRecord
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

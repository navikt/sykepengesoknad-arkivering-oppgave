package no.nav.syfo.service

import no.nav.syfo.domain.Innsending
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.log
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class BehandleFeiledeSoknaderService @Inject
constructor(private val spreOppgaverService: SpreOppgaverService) {
    fun behandleFeiletSoknad(innsending: Innsending?, sykepengesoknad: Sykepengesoknad) {
        try {
            if (innsending?.behandlet != null) {
                log().warn(
                    "Forsøkte å rebehandle ferdigbehandlet søknad med innsendingid: {} og søknadsid: {}",
                    innsending.innsendingsId, sykepengesoknad.id
                )
            } else {
                spreOppgaverService.soknadSendt(sykepengesoknad)
            }
        } catch (e: RuntimeException) {
            throw RuntimeException("Feilet ved rebehandling av innsending med ressursid: ${sykepengesoknad.id}", e)
        }
    }
}

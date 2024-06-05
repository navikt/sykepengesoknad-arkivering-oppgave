package no.nav.helse.flex.spreoppgave

import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.domain.dto.Soknadstype.ARBEIDSLEDIG
import no.nav.helse.flex.domain.dto.Soknadstype.ARBEIDSTAKERE
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.service.SaksbehandlingsService
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SpreOppgaverService(
    private val saksbehandlingsService: SaksbehandlingsService,
    private val spreOppgaveRepository: SpreOppgaveRepository,
    private val handterOppave: HandterOppgaveInterface,
) {
    private val timeoutTimer = (7 * 24).toLong()

    fun prosesserOppgave(
        oppgave: OppgaveDTO,
        kilde: OppgaveKilde,
    ) {
        val eksisterendeOppgave = spreOppgaveRepository.findBySykepengesoknadId(oppgave.dokumentId.toString())
        when (kilde) {
            OppgaveKilde.Søknad -> handterOppave.håndterOppgaveFraSøknad(eksisterendeOppgave, oppgave)
            OppgaveKilde.Saksbehandling -> handterOppave.håndterOppgaveFraBømlo(eksisterendeOppgave, oppgave)
        }
    }

    fun soknadSendt(sykepengesoknad: Sykepengesoknad) {
        try {
            if (sykepengesoknad.status == "SENDT" && !ettersendtTilArbeidsgiver(sykepengesoknad)) {
                val innsendingsId = saksbehandlingsService.behandleSoknad(sykepengesoknad)

                val timeoutMinutter =
                    if (sykepengesoknad.erSoknadSpeilKjennerTil()) {
                        timeoutTimer * 60
                    } else {
                        // Bømlo kjenner ikke disse søknadene. Lar de timeoute etter 1 minutt
                        1
                    }
                if (sykepengesoknad.sendtNav != null) {
                    prosesserOppgave(
                        OppgaveDTO(
                            dokumentId = UUID.fromString(sykepengesoknad.id),
                            dokumentType = DokumentTypeDTO.Søknad,
                            oppdateringstype = OppdateringstypeDTO.VenterPaBomlo,
                            timeout = sykepengesoknad.sendtNav.plusMinutes(timeoutMinutter),
                        ),
                        OppgaveKilde.Søknad,
                    )
                }
                saksbehandlingsService.settFerdigbehandlet(innsendingsId)
            }
        } catch (e: DbActionExecutionException) {
            // Kastes videre for å gjøre acknowledgment.nack()
            throw e
        } catch (e: Exception) {
            saksbehandlingsService.innsendingFeilet(sykepengesoknad, e)
        }
    }

    private fun Sykepengesoknad.erSoknadSpeilKjennerTil(): Boolean {
        return (soknadstype == ARBEIDSTAKERE || soknadstype == ARBEIDSLEDIG) && this.sendTilGosys != true
    }

    private fun ettersendtTilArbeidsgiver(sykepengesoknad: Sykepengesoknad) =
        sykepengesoknad.sendtArbeidsgiver != null &&
            sykepengesoknad.sendtNav?.isBefore(sykepengesoknad.sendtArbeidsgiver) ?: false
}

enum class OppgaveKilde {
    Søknad,
    Saksbehandling,
}

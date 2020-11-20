package no.nav.syfo.kafkaincident

import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.log
import no.nav.syfo.service.SaksbehandlingsService
import no.nav.syfo.service.SpreOppgaverService
import no.nav.syfo.service.ettersendtTilArbeidsgiver
import no.nav.syfo.service.skalBehandlesAvNav
import org.springframework.stereotype.Service

@Service
class SoknadSendtSjekkService(
    private val innsendingDAO: InnsendingDAO,
    private val oppgavestyringDAO: OppgavestyringDAO,
    private val spreOppgaverService: SpreOppgaverService,
    private val saksbehandlingsService: SaksbehandlingsService
) {
    val log = log()

    fun sykepengesoknadSendt(soknad: SykepengesoknadDTO) {
        try {
            val sykepengesoknad = soknad.toSykepengesoknad()

            if (sykepengesoknad.status == "SENDT" && !ettersendtTilArbeidsgiver(sykepengesoknad)) {
                val eksisterendeInnsending = innsendingDAO.finnInnsendingForSykepengesoknad(sykepengesoknad.id) ?: throw Exception("eksisterendeInnsending")
                eksisterendeInnsending.innsendingsId // Denne er alltid satt hvis den over finnes
                eksisterendeInnsending.saksId ?: throw Exception("saksId")
                eksisterendeInnsending.journalpostId ?: throw Exception("journalpostId")

                if (sykepengesoknad.soknadstype == Soknadstype.ARBEIDSTAKERE && skalBehandlesAvNav(sykepengesoknad)) {
                    // Sjekker ikke om det er riktig status her
                    oppgavestyringDAO.hentSpreOppgave(sykepengesoknad.id) ?: throw Exception("spreOppgave")
                } else {
                    if (skalBehandlesAvNav(sykepengesoknad)) {
                        eksisterendeInnsending.oppgaveId ?: throw Exception("oppgaveId")
                    }
                }

                eksisterendeInnsending.behandlet ?: throw Exception("'Behandlings tidspunkt'")
            }
        } catch (e: Exception) {
            val sykepengesoknad = soknad.toSykepengesoknad()
            val spreOppgave = oppgavestyringDAO.hentSpreOppgave(sykepengesoknad.id)

            if (spreOppgave == null) {
                spreOppgaverService.soknadSendt(sykepengesoknad)
                log.info("Rebehandlet ${soknad.id} etter kafka incident")
            } else if (!spreOppgave.avstemt) {
                spreOppgaverService.soknadSendt(sykepengesoknad)
                log.info("Rebehandlet ${soknad.id} etter kafka incident")
            } else {
                saksbehandlingsService.behandleSoknad(sykepengesoknad)
                log.info("Rebehandlet ${soknad.id} etter kafka incident, beholder spre oppgave status ${spreOppgave.status.name} siden den allerede er avstemt")
            }
            // Fortsett til neste
        }
    }
}

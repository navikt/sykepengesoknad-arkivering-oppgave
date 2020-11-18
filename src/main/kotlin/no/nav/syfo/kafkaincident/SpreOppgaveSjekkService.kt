package no.nav.syfo.kafkaincident

import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.log
import org.springframework.stereotype.Service

@Service
class SpreOppgaveSjekkService(
        private val oppgavestyringDAO: OppgavestyringDAO
) {
    val log = log()

    fun sjekk(oppgave: OppgaveDTO) {
        try {
            if (oppgave.dokumentType == DokumentTypeDTO.Søknad) {
                val eksisterendeOppgave = oppgavestyringDAO.hentSpreOppgave(oppgave.dokumentId.toString())
                if (eksisterendeOppgave == null) {
                    throw Exception("Oppgave")
                } else if (oppgave.oppdateringstype == OppdateringstypeDTO.Utsett) {
                    // Kan ha fått nyere status, sjekker ikke her
                } else if (oppgave.oppdateringstype == OppdateringstypeDTO.Ferdigbehandlet) {
                    if (eksisterendeOppgave.status != OppgaveStatus.IkkeOpprett) {
                        throw Exception("Skulle hatt status Ikke Opprett")
                    }
                } else if (oppgave.oppdateringstype == OppdateringstypeDTO.Opprett) {
                    if (eksisterendeOppgave.status != OppgaveStatus.Opprett && eksisterendeOppgave.status != OppgaveStatus.Opprettet) {
                        throw Exception("Skulle hatt status Opprett / Opprettet")
                    }
                }
            }
        } catch (e: Exception) {
            log.info("Spre oppgave ${oppgave.dokumentId} mangler -> ${e.message}")
            // Fortsett til neste
        }
    }
}

package no.nav.helse.flex.tilbakedaterte

import no.nav.helse.flex.service.Oppgave
import no.nav.helse.flex.service.OppgaveClient
import no.nav.syfo.sykmelding.kafka.model.SykmeldingKafkaMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class BehandleSykmelding(
    val oppgaverForTilbakedaterteRepository: OppgaverForTilbakedaterteRepository,
    val oppgaveClient: OppgaveClient
) {


    fun prosesserSykmelding(key: String, melding: SykmeldingKafkaMessage?) {
        if (melding == null) {
            return
        }
        if (melding.sykmelding.merknader?.isNotEmpty() == true) {
            return
        }

        oppgaverForTilbakedaterteRepository.findBySykmeldingUuid(melding.sykmelding.id).forEach {
            if(it.status != "OPPRETTET") {
                return
            }

            val hentetOppgave = oppgaveClient.hentOppgave(it.oppgaveId)
            if (hentetOppgave.kanOppdateres()) {

                oppgaveClient.oppdaterOppgave(it.oppgaveId, hentetOppgave.oppdaterOppgave())
            }
        }

    }

}

private fun Oppgave.kanOppdateres(): Boolean {
    TODO("Not yet implemented")
}

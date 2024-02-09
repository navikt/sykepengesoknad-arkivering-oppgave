package no.nav.helse.flex.tilbakedaterte

import no.nav.helse.flex.service.HentOppgaveResponse
import no.nav.helse.flex.service.OppdaterOppgaveReqeust
import no.nav.helse.flex.service.OppgaveClient
import no.nav.syfo.sykmelding.kafka.model.SykmeldingKafkaMessage
import org.springframework.stereotype.Component

@Component
class BehandleSykmelding(
    val oppgaverForTilbakedaterteRepository: OppgaverForTilbakedaterteRepository,
    val oppgaveClient: OppgaveClient,
) {
    fun prosesserSykmelding(
        key: String,
        melding: SykmeldingKafkaMessage?,
    ) {
        if (melding == null) {
            return
        }
        if (melding.sykmelding.merknader?.isNotEmpty() == true) {
            return
        }

        oppgaverForTilbakedaterteRepository.findBySykmeldingUuid(melding.sykmelding.id).forEach {
            if (it.status != OppgaverForTilbakedaterteStatus.OPPRETTET) {
                return
            }

            val hentetOppgave = oppgaveClient.hentOppgave(it.oppgaveId)
            if (hentetOppgave.kanOppdateres()) {
                val oppdaterOppgaveReqeust = OppdaterOppgaveReqeust(
                    behandlingstema = null,
                    behandlingstype = null
                )
                oppgaveClient.oppdaterOppgave(it.oppgaveId, oppdaterOppgaveReqeust)
            }
        }
    }
}

private fun HentOppgaveResponse.kanOppdateres(): Boolean {
    TODO("Not yet implemented")
}

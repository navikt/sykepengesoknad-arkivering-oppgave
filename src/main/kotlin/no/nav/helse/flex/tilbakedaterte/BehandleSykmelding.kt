package no.nav.helse.flex.tilbakedaterte

import no.nav.helse.flex.client.SykepengesoknadBackendClient
import no.nav.helse.flex.kafka.mapper.toSykepengesoknad
import no.nav.helse.flex.medlemskap.MedlemskapVurdering
import no.nav.helse.flex.oppgave.log
import no.nav.helse.flex.service.*
import no.nav.syfo.sykmelding.kafka.model.SykmeldingKafkaMessage
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class BehandleSykmelding(
    private val oppgaverForTilbakedaterteRepository: OppgaverForTilbakedaterteRepository,
    private val oppgaveClient: OppgaveClient,
    private val medlemskapVurdering: MedlemskapVurdering,
    private val sykepengesoknadBackendClient: SykepengesoknadBackendClient,
    private val identService: IdentService,
) {
    fun prosesserSykmelding(
        key: String,
        melding: SykmeldingKafkaMessage?,
    ) {
        if (melding == null) {
            return
        }

        oppgaverForTilbakedaterteRepository.findBySykmeldingUuid(melding.sykmelding.id).forEach {
            if (it.status != OppgaverForTilbakedaterteStatus.OPPRETTET) {
                return
            }

            if (melding.sykmelding.merknader?.isNotEmpty() == true) {
                oppgaverForTilbakedaterteRepository.save(
                    it.copy(
                        status = OppgaverForTilbakedaterteStatus.IKKE_GODKJENT,
                        oppdatert = Instant.now(),
                    ),
                )
                return@forEach
            }

            val hentetOppgave = oppgaveClient.hentOppgave(it.oppgaveId)
            if (hentetOppgave.kanOppdateres()) {
                val aktorid = identService.hentAktorIdForFnr(melding.kafkaMetadata.fnr)
                val sykepengesoknad =
                    sykepengesoknadBackendClient.hentSoknad(it.sykepengesoknadUuid).toSykepengesoknad(aktorid)

                val medlemskapVurdering = medlemskapVurdering.hentEndeligMedlemskapVurdering(sykepengesoknad)

                val behandlingstemaOgType =
                    finnBehandlingstemaOgType(
                        soknad = sykepengesoknad,
                        harRedusertVenteperiode = sykepengesoknad.harRedusertVenteperiode,
                        speilRelatert = false,
                        medlemskapVurdering = medlemskapVurdering,
                    )

                val oppdaterOppgaveReqeust =
                    OppdaterOppgaveReqeust(
                        behandlingstema = behandlingstemaOgType.behandlingstema,
                        behandlingstype = behandlingstemaOgType.behandlingstype,
                    )
                log.info(
                    "Oppdaterer oppgave ${it.oppgaveId} med behandlingstema ${behandlingstemaOgType.behandlingstema} " +
                        "og behandlingstype ${behandlingstemaOgType.behandlingstype}",
                )
                oppgaveClient.oppdaterOppgave(it.oppgaveId, oppdaterOppgaveReqeust)
                oppgaverForTilbakedaterteRepository.save(
                    it.copy(
                        status = OppgaverForTilbakedaterteStatus.OPPDATERT,
                        oppdatert = Instant.now(),
                    ),
                )
            } else {
                oppgaverForTilbakedaterteRepository.save(
                    it.copy(
                        status = OppgaverForTilbakedaterteStatus.OPPGAVE_ALLEREDE_FERDIGSTILT,
                        oppdatert = Instant.now(),
                    ),
                )
            }
        }
    }
}

private fun HentOppgaveResponse.kanOppdateres(): Boolean {
    if (status == "FERDIGSTILT") {
        return false
    }
    if (status == "FEILREGISTRERT") {
        return false
    }
    return true
}

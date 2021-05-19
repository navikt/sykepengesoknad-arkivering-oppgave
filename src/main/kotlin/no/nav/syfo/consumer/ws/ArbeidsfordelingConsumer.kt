package no.nav.syfo.consumer.ws

import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Soknadstype.OPPHOLD_UTLAND
import no.nav.syfo.logger
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnBehandlendeEnhetListeUgyldigInput
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.ArbeidsfordelingKriterier
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Behandlingstema
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Diskresjonskoder
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Enhetsstatus
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Geografi
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Tema
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest
import org.springframework.stereotype.Component

const val BEHANDLINGSTEMA_OPPHOLD_UTLAND = "ab0314"
const val NAV_OPPFOLGING_UTLAND_KONTOR_NR = "0393"

@Component
class ArbeidsfordelingConsumer(private val arbeidsfordelingV1: ArbeidsfordelingV1) {
    private val log = logger()

    fun finnBehandlendeEnhet(tilknytning: GeografiskTilknytning, soknadstype: Soknadstype): String {
        try {
            return if ("NOR" == tilknytning.geografiskTilknytning)
                "4474"
            else
                arbeidsfordelingV1.finnBehandlendeEnhetListe(
                    FinnBehandlendeEnhetListeRequest().apply {
                        arbeidsfordelingKriterier = ArbeidsfordelingKriterier().apply {
                            diskresjonskode = if (tilknytning.diskresjonskode == null) null else Diskresjonskoder().apply {
                                value = tilknytning.diskresjonskode
                            }
                            geografiskTilknytning = Geografi().apply {
                                value = tilknytning.geografiskTilknytning
                            }
                            tema = Tema().apply {
                                value = "SYK"
                            }
                            behandlingstema = hentRiktigTemaBehandlingstemaForSoknadstype(soknadstype)
                        }
                    }
                )
                    .behandlendeEnhetListe
                    .firstOrNull { organisasjonsenhet -> organisasjonsenhet.status == Enhetsstatus.AKTIV }?.enhetId
                    ?: throw RuntimeException("Fant ingen aktiv enhet for ${tilknytning.geografiskTilknytning}")
        } catch (e: FinnBehandlendeEnhetListeUgyldigInput) {
            log.error("Feil ved henting av brukers forvaltningsenhet", e)
            throw RuntimeException("Feil ved henting av brukers forvaltningsenhet", e)
        } catch (e: RuntimeException) {
            log.warn(
                "Klarte ikke å hente behandlende enhet! Gir oppgaven til NAV_OPPFOLGING_UTLAND ($NAV_OPPFOLGING_UTLAND_KONTOR_NR)",
                e
            )
            return NAV_OPPFOLGING_UTLAND_KONTOR_NR
        }
    }

    private fun hentRiktigTemaBehandlingstemaForSoknadstype(soknadstype: Soknadstype?): Behandlingstema? {
        return if (soknadstype === OPPHOLD_UTLAND) {
            Behandlingstema().apply {
                value = BEHANDLINGSTEMA_OPPHOLD_UTLAND
            }
        } else null
    }
}

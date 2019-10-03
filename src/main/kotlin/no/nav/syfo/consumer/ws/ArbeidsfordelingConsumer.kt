package no.nav.syfo.consumer.ws

import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Soknadstype.OPPHOLD_UTLAND
import no.nav.syfo.log
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnBehandlendeEnhetListeUgyldigInput
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.*
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSEnhetsstatus.AKTIV
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeRequest
import org.springframework.stereotype.Component

const val BEHANDLINGSTEMA_OPPHOLD_UTLAND = "ab0314"
const val NAV_OPPFOLGING_UTLAND_KONTOR_NR = "0393"

@Component
class ArbeidsfordelingConsumer(private val arbeidsfordelingV1: ArbeidsfordelingV1) {

    fun finnBehandlendeEnhet(geografiskTilknytning: GeografiskTilknytning, soknadstype: Soknadstype?): String {
        try {
            return if ("NOR" == geografiskTilknytning.geografiskTilknytning)
                "4474"
            else
                arbeidsfordelingV1.finnBehandlendeEnhetListe(WSFinnBehandlendeEnhetListeRequest()
                        .withArbeidsfordelingKriterier(WSArbeidsfordelingKriterier()
                            .withDiskresjonskode(if (geografiskTilknytning.diskresjonskode == null) null else WSDiskresjonskoder().withValue(geografiskTilknytning.diskresjonskode))
                            .withGeografiskTilknytning(WSGeografi().withValue(geografiskTilknytning.geografiskTilknytning))
                            .withTema(WSTema().withValue("SYK"))
                            .withBehandlingstema(hentRiktigTemaBehandlingstemaForSoknadstype(soknadstype))))
                        .behandlendeEnhetListe
                        .filter { wsOrganisasjonsenhet -> wsOrganisasjonsenhet.status == AKTIV}
                        .map { wsOrganisasjonsenhet -> wsOrganisasjonsenhet.enhetId }
                        .firstOrNull()
                        ?: throw RuntimeException("Fant ingen aktiv enhet for " + geografiskTilknytning.geografiskTilknytning)
        } catch (e: FinnBehandlendeEnhetListeUgyldigInput) {
            log().error("Feil ved henting av brukers forvaltningsenhet", e)
            throw RuntimeException("Feil ved henting av brukers forvaltningsenhet", e)
        } catch (e: RuntimeException) {
            log().warn("Klarte ikke å hente behandlende enhet! Gir oppgaven til NAV_OPPFOLGING_UTLAND (${NAV_OPPFOLGING_UTLAND_KONTOR_NR})", e)
            return NAV_OPPFOLGING_UTLAND_KONTOR_NR
        }
    }

    private fun hentRiktigTemaBehandlingstemaForSoknadstype(soknadstype: Soknadstype?): WSBehandlingstema? {
        return if (soknadstype === OPPHOLD_UTLAND) {
            WSBehandlingstema().withValue(BEHANDLINGSTEMA_OPPHOLD_UTLAND)
        } else null
    }
}

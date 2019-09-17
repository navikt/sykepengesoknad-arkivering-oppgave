package no.nav.syfo.mock

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnAlleBehandlendeEnheterListeUgyldigInput
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnBehandlendeEnhetListeUgyldigInput
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnAlleBehandlendeEnheterListeRequest
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnAlleBehandlendeEnheterListeResponse
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeRequest
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = ["mockWS"], havingValue = "true")
class ArbeidsfordelingMock : ArbeidsfordelingV1 {

    @Throws(FinnAlleBehandlendeEnheterListeUgyldigInput::class)
    override fun finnAlleBehandlendeEnheterListe(wsFinnAlleBehandlendeEnheterListeRequest: WSFinnAlleBehandlendeEnheterListeRequest): WSFinnAlleBehandlendeEnheterListeResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(FinnBehandlendeEnhetListeUgyldigInput::class)
    override fun finnBehandlendeEnhetListe(wsFinnBehandlendeEnhetListeRequest: WSFinnBehandlendeEnhetListeRequest): WSFinnBehandlendeEnhetListeResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun ping() {}
}

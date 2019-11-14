package no.nav.syfo.mock

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnAlleBehandlendeEnheterListeUgyldigInput
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnBehandlendeEnhetListeUgyldigInput
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeRequest
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeResponse
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = ["mockWS"], havingValue = "true")
class ArbeidsfordelingMock : ArbeidsfordelingV1 {

    @Throws(FinnAlleBehandlendeEnheterListeUgyldigInput::class)
    override fun finnAlleBehandlendeEnheterListe(
        wsFinnAlleBehandlendeEnheterListeRequest: FinnAlleBehandlendeEnheterListeRequest
    ): FinnAlleBehandlendeEnheterListeResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(FinnBehandlendeEnhetListeUgyldigInput::class)
    override fun finnBehandlendeEnhetListe(
        wsFinnBehandlendeEnhetListeRequest: FinnBehandlendeEnhetListeRequest
    ): FinnBehandlendeEnhetListeResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun ping() {}
}

package no.nav.syfo.mock;

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnAlleBehandlendeEnheterListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnAlleBehandlendeEnheterListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnAlleBehandlendeEnheterListeResponse;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "mockWS", havingValue = "true")
public class ArbeidsfordelingMock implements ArbeidsfordelingV1 {

    public WSFinnAlleBehandlendeEnheterListeResponse finnAlleBehandlendeEnheterListe(WSFinnAlleBehandlendeEnheterListeRequest wsFinnAlleBehandlendeEnheterListeRequest) throws FinnAlleBehandlendeEnheterListeUgyldigInput {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSFinnBehandlendeEnhetListeResponse finnBehandlendeEnhetListe(WSFinnBehandlendeEnhetListeRequest wsFinnBehandlendeEnhetListeRequest) throws FinnBehandlendeEnhetListeUgyldigInput {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public void ping() { }
}

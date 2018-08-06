package no.nav.syfo.mock;

import no.nav.tjeneste.virksomhet.behandlesak.v1.BehandleSakV1;
import no.nav.tjeneste.virksomhet.behandlesak.v1.OpprettSakSakEksistererAllerede;
import no.nav.tjeneste.virksomhet.behandlesak.v1.OpprettSakUgyldigInput;
import no.nav.tjeneste.virksomhet.behandlesak.v1.meldinger.WSOpprettSakRequest;
import no.nav.tjeneste.virksomhet.behandlesak.v1.meldinger.WSOpprettSakResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "mockWS", havingValue = "true")
public class BehandleSakMock implements BehandleSakV1 {

    public WSOpprettSakResponse opprettSak(WSOpprettSakRequest wsOpprettSakRequest) throws OpprettSakSakEksistererAllerede, OpprettSakUgyldigInput {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public void ping() { }
}

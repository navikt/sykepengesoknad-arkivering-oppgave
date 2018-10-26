package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.ws.ArbeidsfordelingConsumer;
import no.nav.syfo.consumer.ws.GeografiskTilknytning;
import no.nav.syfo.consumer.ws.PersonConsumer;
import no.nav.syfo.domain.dto.Soknadstype;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BehandlendeEnhetService {

    private PersonConsumer personConsumer;
    private ArbeidsfordelingConsumer arbeidsfordelingConsumer;

    public BehandlendeEnhetService(PersonConsumer personConsumer, ArbeidsfordelingConsumer arbeidsfordelingConsumer) {
        this.personConsumer = personConsumer;
        this.arbeidsfordelingConsumer = arbeidsfordelingConsumer;
    }

    public String hentBehandlendeEnhet(String fnr, Soknadstype soknadstype) {
        GeografiskTilknytning geografiskTilknytning = personConsumer.hentGeografiskTilknytning(fnr);

        return arbeidsfordelingConsumer.finnBehandlendeEnhet(geografiskTilknytning, soknadstype);
    }
}

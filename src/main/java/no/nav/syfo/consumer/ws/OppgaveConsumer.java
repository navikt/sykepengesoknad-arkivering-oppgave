package no.nav.syfo.consumer.ws;

import no.nav.syfo.domain.Oppgave;
import no.nav.tjeneste.virksomhet.oppgave.v3.OppgaveV3;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;

@Component
public class OppgaveConsumer {

    private final OppgaveV3 oppgaveV3;

    @Inject
    public OppgaveConsumer(OppgaveV3 oppgaveV3) {
        this.oppgaveV3 = oppgaveV3;
    }

    public Optional<Oppgave> finnOppgave() {
        return Optional.empty();
    }
}

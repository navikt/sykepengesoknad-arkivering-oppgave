package no.nav.syfo.mock;

import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.*;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "mockWS", havingValue = "true")
public class OppgavebehandlingMock implements OppgavebehandlingV3 {

    public WSOpprettMappeResponse opprettMappe(WSOpprettMappeRequest wsOpprettMappeRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public void lagreOppgave(WSLagreOppgaveRequest wsLagreOppgaveRequest) throws LagreOppgaveOppgaveIkkeFunnet, LagreOppgaveOptimistiskLasing {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSTildelOppgaveResponse tildelOppgave(WSTildelOppgaveRequest wsTildelOppgaveRequest) throws TildelOppgaveUgyldigInput {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSOpprettOppgaveBolkResponse opprettOppgaveBolk(WSOpprettOppgaveBolkRequest wsOpprettOppgaveBolkRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSFerdigstillOppgaveBolkResponse ferdigstillOppgaveBolk(WSFerdigstillOppgaveBolkRequest wsFerdigstillOppgaveBolkRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSLagreOppgaveBolkResponse lagreOppgaveBolk(WSLagreOppgaveBolkRequest wsLagreOppgaveBolkRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSOpprettOppgaveResponse opprettOppgave(WSOpprettOppgaveRequest wsOpprettOppgaveRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public void feilregistrerOppgave(WSFeilregistrerOppgaveRequest wsFeilregistrerOppgaveRequest) throws FeilregistrerOppgaveOppgaveIkkeFunnet, FeilregistrerOppgaveUlovligStatusOvergang {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public void lagreMappe(WSLagreMappeRequest wsLagreMappeRequest) throws LagreMappeMappeIkkeFunnet {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public void slettMappe(WSSlettMappeRequest wsSlettMappeRequest) throws SlettMappeMappeIkkeFunnet, SlettMappeMappeIkkeTom {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public void ping() { }
}

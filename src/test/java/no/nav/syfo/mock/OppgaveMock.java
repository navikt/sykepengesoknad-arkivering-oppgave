package no.nav.syfo.mock;

import no.nav.tjeneste.virksomhet.oppgave.v3.HentOppgaveOppgaveIkkeFunnet;
import no.nav.tjeneste.virksomhet.oppgave.v3.OppgaveV3;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "mockWS", havingValue = "true")
public class OppgaveMock implements OppgaveV3 {

    public WSFinnFerdigstiltOppgaveListeResponse finnFerdigstiltOppgaveListe(WSFinnFerdigstiltOppgaveListeRequest wsFinnFerdigstiltOppgaveListeRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSFinnOppgaveListeResponse finnOppgaveListe(WSFinnOppgaveListeRequest wsFinnOppgaveListeRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSFinnFeilregistrertOppgaveListeResponse finnFeilregistrertOppgaveListe(WSFinnFeilregistrertOppgaveListeRequest wsFinnFeilregistrertOppgaveListeRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSFinnMappeListeResponse finnMappeListe(WSFinnMappeListeRequest wsFinnMappeListeRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSHentOppgaveResponse hentOppgave(WSHentOppgaveRequest wsHentOppgaveRequest) throws HentOppgaveOppgaveIkkeFunnet {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public void ping() { }
}

package no.nav.syfo.consumer.ws;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.OppgavebehandlingV3;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.meldinger.WSOpprettOppgave;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.meldinger.WSOpprettOppgaveRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static java.time.LocalDate.now;

@Component
@Slf4j
public class OppgavebehandlingConsumer {
    private final OppgavebehandlingV3 oppgavebehandlingV3;

    @Inject
    public OppgavebehandlingConsumer(OppgavebehandlingV3 oppgavebehandlingV3) {
        this.oppgavebehandlingV3 = oppgavebehandlingV3;
    }

    public String opprettOppgave(String fnr, String behandlendeEnhet, String saksId, String journalpostId, String beskrivelse, Soknadstype soknadstype) {
        try {
            String oppgaveId = oppgavebehandlingV3.opprettOppgave(new WSOpprettOppgaveRequest()
                    .withOpprettetAvEnhetId(9999)
                    .withOpprettOppgave(new WSOpprettOppgave()
                            .withBrukerId(fnr)
                            .withBrukertypeKode("PERSON")
                            .withOppgavetypeKode("SOK_SYK")
                            .withFagomradeKode("SYK")
                            .withUnderkategoriKode(getUnderkategoriKodeForSoknadstype(soknadstype))
                            .withPrioritetKode("NORM_SYK")
                            .withBeskrivelse(beskrivelse)
                            .withAktivFra(now())
                            .withAktivTil(now().plusDays(7))
                            .withAnsvarligEnhetId(behandlendeEnhet)
                            .withDokumentId(journalpostId)
                            .withMottattDato(now())
                            .withSaksnummer(saksId)
                            .withOppfolging("\nDu kan gi oss tilbakemelding på søknaden om sykepenger.\n" +
                                    "Gå til internettadresse: nav.no/digitalsykmelding/tilbakemelding")
                    )).getOppgaveId();
            return oppgaveId;
        } catch (RuntimeException e) {
            log.error("Klarte ikke å opprette oppgave. ", e);
            throw new RuntimeException(e);
        }
    }

    public String getUnderkategoriKodeForSoknadstype(Soknadstype soknadstype){
        if(soknadstype == Soknadstype.OPPHOLD_UTLAND){
            return "UTENLANDSOPP_SYK";
        }
        return "SYK_SYK";
    }
}

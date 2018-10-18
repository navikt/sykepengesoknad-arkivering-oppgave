package no.nav.syfo.consumer.ws;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.OppgavebehandlingV3;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.meldinger.WSOpprettOppgave;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.meldinger.WSOpprettOppgaveRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static no.nav.syfo.service.BeskrivelseService.lagBeskrivelse;

@Component
@Slf4j
public class OppgavebehandlingConsumer {
    private final OppgavebehandlingV3 oppgavebehandlingV3;

    @Inject
    public OppgavebehandlingConsumer(OppgavebehandlingV3 oppgavebehandlingV3) {
        this.oppgavebehandlingV3 = oppgavebehandlingV3;
    }

    public String opprettOppgave(String fnr, String behandlendeEnhet, String saksId, String journalpostId, Soknad soknad) {
        try {
            return oppgavebehandlingV3.opprettOppgave(new WSOpprettOppgaveRequest()
                    .withOpprettetAvEnhetId(9999)
                    .withOpprettOppgave(new WSOpprettOppgave()
                            .withBrukerId(fnr)
                            .withBrukertypeKode("PERSON")
                            .withOppgavetypeKode("SOK_SYK")
                            .withFagomradeKode("SYK")
                            .withUnderkategoriKode(getUnderkategoriKodeForSoknadstype(soknad.getSoknadstype()))
                            .withPrioritetKode("NORM_SYK")
                            .withBeskrivelse(lagBeskrivelse(soknad))
                            .withAktivFra(now())
                            .withAktivTil(omTreUkedager(now()))
                            .withAnsvarligEnhetId(behandlendeEnhet)
                            .withDokumentId(journalpostId)
                            .withMottattDato(now())
                            .withSaksnummer(saksId)
                            .withOppfolging("\nDu kan gi oss tilbakemelding på søknaden om sykepenger.\n" +
                                    "Gå til internettadresse: nav.no/digitalsykmelding/tilbakemelding")
                    )).getOppgaveId();
        } catch (RuntimeException e) {
            log.error("Klarte ikke å opprette oppgave. ", e);
            throw new RuntimeException(e);
        }
    }

    LocalDate omTreUkedager(final LocalDate idag) {
        switch (idag.getDayOfWeek()) {
            case SUNDAY:
                return idag.plusDays(4);
            case MONDAY:
            case TUESDAY:
                return idag.plusDays(3);
            default:
                return idag.plusDays(5);
        }
    }

    private String getUnderkategoriKodeForSoknadstype(Soknadstype soknadstype) {
        if (soknadstype == Soknadstype.OPPHOLD_UTLAND) {
            return "UTENLANDSOPP_SYK";
        }
        return "SYK_SYK";
    }
}

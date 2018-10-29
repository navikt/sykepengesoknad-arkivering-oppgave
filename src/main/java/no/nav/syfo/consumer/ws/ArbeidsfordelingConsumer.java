package no.nav.syfo.consumer.ws;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeRequest;
import org.springframework.stereotype.Component;

import static no.nav.syfo.domain.dto.Soknadstype.OPPHOLD_UTLAND;
import static no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.WSEnhetsstatus.AKTIV;

@Component
@Slf4j
public class ArbeidsfordelingConsumer {

    private ArbeidsfordelingV1 arbeidsfordelingV1;

    public static final String BEHANDLINGSTEMA_OPPHOLD_UTLAND = "ab0314";

    public ArbeidsfordelingConsumer(ArbeidsfordelingV1 arbeidsfordelingV1) {
        this.arbeidsfordelingV1 = arbeidsfordelingV1;
    }

    public String finnBehandlendeEnhet(GeografiskTilknytning geografiskTilknytning, Soknadstype soknadstype) {
        try {
            return arbeidsfordelingV1.finnBehandlendeEnhetListe(new WSFinnBehandlendeEnhetListeRequest()
                    .withArbeidsfordelingKriterier(new WSArbeidsfordelingKriterier()
                            .withDiskresjonskode(geografiskTilknytning.diskresjonskode != null ? new WSDiskresjonskoder().withValue(geografiskTilknytning.diskresjonskode) : null)
                            .withGeografiskTilknytning(new WSGeografi().withValue(geografiskTilknytning.geografiskTilknytning))
                            .withTema(new WSTema().withValue("SYK"))
                            .withBehandlingstema(hentRiktigTemaBehandlingstemaForSoknadstype(soknadstype))))
                    .getBehandlendeEnhetListe()
                    .stream()
                    .filter(wsOrganisasjonsenhet -> AKTIV.equals(wsOrganisasjonsenhet.getStatus()))
                    .map(WSOrganisasjonsenhet::getEnhetId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Fant ingen aktiv enhet for " + geografiskTilknytning));
        } catch (FinnBehandlendeEnhetListeUgyldigInput e) {
            log.error("Feil ved henting av brukers forvaltningsenhet", e);
            throw new RuntimeException("Feil ved henting av brukers forvaltningsenhet", e);
        } catch (RuntimeException e) {
            log.error("Klarte ikke å hente behandlende enhet!", e);
            throw new RuntimeException(e);
        }
    }

    private WSBehandlingstema hentRiktigTemaBehandlingstemaForSoknadstype(Soknadstype soknadstype) {
        if (soknadstype == OPPHOLD_UTLAND) {
            return new WSBehandlingstema().withValue(BEHANDLINGSTEMA_OPPHOLD_UTLAND);
        }
        return null;
    }
}

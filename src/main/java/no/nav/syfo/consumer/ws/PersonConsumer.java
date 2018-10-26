package no.nav.syfo.consumer.ws;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static net.logstash.logback.encoder.org.apache.commons.lang.WordUtils.capitalizeFully;
import static org.springframework.util.StringUtils.isEmpty;

@Component
@Slf4j
public class PersonConsumer {

    private PersonV3 personV3;

    @Inject
    public PersonConsumer(PersonV3 personV3) {
        this.personV3 = personV3;
    }


    public String finnBrukerPersonnavnByFnr(String fnr) {
        return personV3.hentPersonnavnBolk(new HentPersonnavnBolkRequest()
                .withAktoerListe(new PersonIdent().withIdent(new NorskIdent().withIdent(fnr))))
                .getAktoerHarNavnListe()
                .stream()
                .map(AktoerHarNavn::getPersonnavn)
                .map(this::fulltNavn)
                .findFirst()
                .orElse(null);
    }

    public GeografiskTilknytning hentGeografiskTilknytning(String fnr) {
        try {
            HentGeografiskTilknytningResponse response = personV3.hentGeografiskTilknytning(new HentGeografiskTilknytningRequest()
                    .withAktoer(new PersonIdent().withIdent(new NorskIdent().withIdent(fnr))));

            return GeografiskTilknytning
                    .builder()
                    .geografiskTilknytning(of(response.getGeografiskTilknytning()).map(geografiskTilknytning -> geografiskTilknytning.getGeografiskTilknytning()).orElse(null))
                    .diskresjonskode(ofNullable(response.getDiskresjonskode()).map(Kodeverdi::getValue).orElse(null))
                    .build();
        } catch (HentGeografiskTilknytningSikkerhetsbegrensing | HentGeografiskTilknytningPersonIkkeFunnet e) {
            log.error("Feil ved henting av geografisk tilknytning", e);
            throw new RuntimeException("Feil ved henting av geografisk tilknytning", e);
        }
    }

    private String fulltNavn(Personnavn personnavn) {
        String navn;
        if (isEmpty(personnavn.getFornavn())) {
            navn = personnavn.getEtternavn();
        } else if (isEmpty(personnavn.getMellomnavn())) {
            navn = (personnavn.getFornavn() + " " + personnavn.getEtternavn());
        } else {
            navn = (personnavn.getFornavn() + " " + personnavn.getMellomnavn() + " " + personnavn.getEtternavn());
        }

        char[] delimiters = {' ', '-'};
        return capitalizeFully(navn, delimiters);
    }
}

package no.nav.syfo.consumer.ws;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.AktoerHarNavn;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static java.text.MessageFormat.format;
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
        log.info(fnr);
        return personV3.hentPersonnavnBolk(new HentPersonnavnBolkRequest()
                .withAktoerListe(new PersonIdent().withIdent(new NorskIdent().withIdent(fnr))))
                .getAktoerHarNavnListe()
                .stream()
                .map(AktoerHarNavn::getPersonnavn)
                .map(this::fulltNavn)
                .findFirst()
                .orElse(null);
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

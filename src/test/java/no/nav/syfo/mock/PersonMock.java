package no.nav.syfo.mock;

import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "mockWS", havingValue = "true")
public class PersonMock implements PersonV3 {

    public HentPersonResponse hentPerson(HentPersonRequest hentPersonRequest) throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public HentPersonnavnBolkResponse hentPersonnavnBolk(HentPersonnavnBolkRequest hentPersonnavnBolkRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public HentSikkerhetstiltakResponse hentSikkerhetstiltak(HentSikkerhetstiltakRequest hentSikkerhetstiltakRequest) throws HentSikkerhetstiltakPersonIkkeFunnet {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public HentGeografiskTilknytningResponse hentGeografiskTilknytning(HentGeografiskTilknytningRequest hentGeografiskTilknytningRequest) throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public HentVergeResponse hentVerge(HentVergeRequest hentVergeRequest) throws HentVergePersonIkkeFunnet, HentVergeSikkerhetsbegrensning {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public HentEkteskapshistorikkResponse hentEkteskapshistorikk(HentEkteskapshistorikkRequest hentEkteskapshistorikkRequest) throws HentEkteskapshistorikkPersonIkkeFunnet, HentEkteskapshistorikkSikkerhetsbegrensning {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public HentPersonerMedSammeAdresseResponse hentPersonerMedSammeAdresse(HentPersonerMedSammeAdresseRequest hentPersonerMedSammeAdresseRequest) throws HentPersonerMedSammeAdresseIkkeFunnet, HentPersonerMedSammeAdresseSikkerhetsbegrensning {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public void ping() { }
}

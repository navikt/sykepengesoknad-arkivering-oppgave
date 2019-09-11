package no.nav.syfo.mock

import no.nav.tjeneste.virksomhet.person.v3.binding.*
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = ["mockWS"], havingValue = "true")
class PersonMock : PersonV3 {

    @Throws(HentPersonPersonIkkeFunnet::class, HentPersonSikkerhetsbegrensning::class)
    override fun hentPerson(hentPersonRequest: HentPersonRequest): HentPersonResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun hentPersonnavnBolk(hentPersonnavnBolkRequest: HentPersonnavnBolkRequest): HentPersonnavnBolkResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(HentSikkerhetstiltakPersonIkkeFunnet::class)
    override fun hentSikkerhetstiltak(hentSikkerhetstiltakRequest: HentSikkerhetstiltakRequest): HentSikkerhetstiltakResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(HentGeografiskTilknytningPersonIkkeFunnet::class, HentGeografiskTilknytningSikkerhetsbegrensing::class)
    override fun hentGeografiskTilknytning(hentGeografiskTilknytningRequest: HentGeografiskTilknytningRequest): HentGeografiskTilknytningResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(HentVergePersonIkkeFunnet::class, HentVergeSikkerhetsbegrensning::class)
    override fun hentVerge(hentVergeRequest: HentVergeRequest): HentVergeResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(HentEkteskapshistorikkPersonIkkeFunnet::class, HentEkteskapshistorikkSikkerhetsbegrensning::class)
    override fun hentEkteskapshistorikk(hentEkteskapshistorikkRequest: HentEkteskapshistorikkRequest): HentEkteskapshistorikkResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(HentPersonerMedSammeAdresseIkkeFunnet::class, HentPersonerMedSammeAdresseSikkerhetsbegrensning::class)
    override fun hentPersonerMedSammeAdresse(hentPersonerMedSammeAdresseRequest: HentPersonerMedSammeAdresseRequest): HentPersonerMedSammeAdresseResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun ping() {}
}

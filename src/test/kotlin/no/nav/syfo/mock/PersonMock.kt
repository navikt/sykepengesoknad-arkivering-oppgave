package no.nav.syfo.mock

import no.nav.tjeneste.virksomhet.person.v3.binding.*
import no.nav.tjeneste.virksomhet.person.v3.informasjon.AktoerHarNavn
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bydel
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn
import no.nav.tjeneste.virksomhet.person.v3.meldinger.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = ["mockWS"], havingValue = "true")
class PersonMock : PersonV3 {

    var returnerKode6 = false

    override fun hentPersonhistorikk(p0: HentPersonhistorikkRequest?): HentPersonhistorikkResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(HentPersonPersonIkkeFunnet::class, HentPersonSikkerhetsbegrensning::class)
    override fun hentPerson(hentPersonRequest: HentPersonRequest): HentPersonResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun hentPersonnavnBolk(hentPersonnavnBolkRequest: HentPersonnavnBolkRequest): HentPersonnavnBolkResponse {
        return HentPersonnavnBolkResponse().withAktoerHarNavnListe(AktoerHarNavn().withPersonnavn(Personnavn().withFornavn("Tom").withEtternavn("Eke")))
    }

    @Throws(HentSikkerhetstiltakPersonIkkeFunnet::class)
    override fun hentSikkerhetstiltak(hentSikkerhetstiltakRequest: HentSikkerhetstiltakRequest): HentSikkerhetstiltakResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(HentGeografiskTilknytningPersonIkkeFunnet::class, HentGeografiskTilknytningSikkerhetsbegrensing::class)
    override fun hentGeografiskTilknytning(hentGeografiskTilknytningRequest: HentGeografiskTilknytningRequest): HentGeografiskTilknytningResponse {
        return HentGeografiskTilknytningResponse().withGeografiskTilknytning(Bydel().withGeografiskTilknytning("Grorud")).also {
            if (returnerKode6) {
                it.withDiskresjonskode(Diskresjonskoder().withValue("SPSF"))
            }
        }
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

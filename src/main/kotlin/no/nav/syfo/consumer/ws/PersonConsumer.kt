package no.nav.syfo.consumer.ws

import net.logstash.logback.encoder.org.apache.commons.lang.WordUtils.capitalizeFully
import no.nav.syfo.log
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personnavn
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonnavnBolkRequest
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class PersonConsumer @Inject
constructor(private val personV3: PersonV3) {


    fun finnBrukerPersonnavnByFnr(fnr: String): String {
        return personV3.hentPersonnavnBolk(HentPersonnavnBolkRequest()
                .withAktoerListe(PersonIdent().withIdent(NorskIdent().withIdent(fnr))))
                .aktoerHarNavnListe
                .map { it.personnavn }
                .map { fulltNavn(it) }
                .first()
    }

    fun hentGeografiskTilknytning(fnr: String): GeografiskTilknytning {
        try {
            val response = personV3.hentGeografiskTilknytning(HentGeografiskTilknytningRequest()
                    .withAktoer(PersonIdent()
                    .withIdent(NorskIdent()
                    .withIdent(fnr))))

            return GeografiskTilknytning(response.geografiskTilknytning?.geografiskTilknytning, response.diskresjonskode?.value)


        } catch (e: HentGeografiskTilknytningSikkerhetsbegrensing) {
            log().error("Feil ved henting av geografisk tilknytning", e)
            throw RuntimeException("Feil ved henting av geografisk tilknytning", e)
        } catch (e: HentGeografiskTilknytningPersonIkkeFunnet) {
            log().error("Feil ved henting av geografisk tilknytning", e)
            throw RuntimeException("Feil ved henting av geografisk tilknytning", e)
        }

    }

    private fun fulltNavn(personnavn: Personnavn): String {
        val navn: String = when {
            personnavn.fornavn.isNullOrBlank() -> personnavn.etternavn
            personnavn.mellomnavn.isNullOrBlank() -> personnavn.fornavn + " " + personnavn.etternavn
            else -> personnavn.fornavn + " " + personnavn.mellomnavn + " " + personnavn.etternavn
        }

        val delimiters = charArrayOf(' ', '-')
        return capitalizeFully(navn, delimiters)
    }
}

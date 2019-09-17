package no.nav.syfo.service

import no.nav.syfo.consumer.ws.ArbeidsfordelingConsumer
import no.nav.syfo.consumer.ws.PersonConsumer
import no.nav.syfo.domain.dto.Soknadstype
import org.springframework.stereotype.Component

@Component
class BehandlendeEnhetService(private val personConsumer: PersonConsumer, private val arbeidsfordelingConsumer: ArbeidsfordelingConsumer) {

    fun hentBehandlendeEnhet(fnr: String, soknadstype: Soknadstype?): String {
        val geografiskTilknytning = personConsumer.hentGeografiskTilknytning(fnr)

        return arbeidsfordelingConsumer.finnBehandlendeEnhet(geografiskTilknytning, soknadstype)
    }
}

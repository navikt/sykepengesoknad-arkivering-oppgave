package no.nav.syfo.service

import no.nav.syfo.consumer.ws.ArbeidsfordelingConsumer
import no.nav.syfo.consumer.ws.PersonConsumer
import no.nav.syfo.domain.dto.Soknadstype
import org.springframework.stereotype.Component

const val NAV_VIKAFOSSEN = "2103"

@Component
class BehandlendeEnhetService(
    private val personConsumer: PersonConsumer,
    private val arbeidsfordelingConsumer: ArbeidsfordelingConsumer
) {

    fun hentBehandlendeEnhet(fnr: String, soknadstype: Soknadstype): String {
        val geografiskTilknytning = personConsumer.hentGeografiskTilknytning(fnr)

        if (soknadstype == Soknadstype.REISETILSKUDD) {
            if (geografiskTilknytning.diskresjonskode == "6") {
                return NAV_VIKAFOSSEN
            }
            return "4488"
        }

        return arbeidsfordelingConsumer.finnBehandlendeEnhet(geografiskTilknytning, soknadstype)
    }
}

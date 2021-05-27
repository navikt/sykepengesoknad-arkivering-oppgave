package no.nav.syfo.service

import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.pdl.finnGT
import no.nav.syfo.client.pdl.getDiskresjonskode
import no.nav.syfo.consumer.ws.ArbeidsfordelingConsumer
import no.nav.syfo.consumer.ws.PersonConsumer
import no.nav.syfo.domain.dto.Soknadstype
import org.springframework.stereotype.Component

const val NAV_VIKAFOSSEN = "2103"
const val DISKRESJONSKODE_KODE6 = "SPSF"
const val DISKRESJONSKODE_KODE7 = "SPFO"

@Component
class BehandlendeEnhetService(
    private val personConsumer: PersonConsumer,
    private val arbeidsfordelingConsumer: ArbeidsfordelingConsumer,
    private val pdlClient: PdlClient,
) {

    fun hentBehandlendeEnhet(fnr: String, soknadstype: Soknadstype): String {
        val geografiskTilknytning = personConsumer.hentGeografiskTilknytning(fnr)

        val pdlRes = pdlClient.hentGeografiskTilknytning(fnr)
        val pdlGT = pdlRes.hentGeografiskTilknytning.finnGT()
        val diskresjonskode = pdlRes.hentPerson.getDiskresjonskode()

        if (geografiskTilknytning.geografiskTilknytning != pdlGT) {
            log.warn("PV3 = ${geografiskTilknytning.geografiskTilknytning}, PDL = $pdlGT")
        }

        if (diskresjonskode != geografiskTilknytning.diskresjonskode) {
            log.warn("PV3 og PDL gir ikke samme resultat for hentGeografiskTilknytning")
        }

        if (soknadstype == Soknadstype.REISETILSKUDD) {
            if (geografiskTilknytning.diskresjonskode == DISKRESJONSKODE_KODE6) {
                return NAV_VIKAFOSSEN
            }
            return "4488"
        }

        return arbeidsfordelingConsumer.finnBehandlendeEnhet(geografiskTilknytning, soknadstype)
    }
}

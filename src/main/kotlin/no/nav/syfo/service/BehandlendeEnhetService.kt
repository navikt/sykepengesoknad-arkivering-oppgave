package no.nav.syfo.service

import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.pdl.finnGT
import no.nav.syfo.client.pdl.getDiskresjonskode
import no.nav.syfo.consumer.ws.ArbeidsfordelingConsumer
import no.nav.syfo.consumer.ws.GeografiskTilknytning
import no.nav.syfo.domain.dto.Soknadstype
import org.springframework.stereotype.Component

const val NAV_VIKAFOSSEN = "2103"
const val DISKRESJONSKODE_KODE6 = "SPSF"
const val DISKRESJONSKODE_KODE7 = "SPFO"

@Component
class BehandlendeEnhetService(
    private val arbeidsfordelingConsumer: ArbeidsfordelingConsumer,
    private val pdlClient: PdlClient,
) {

    fun hentBehandlendeEnhet(fnr: String, soknadstype: Soknadstype, soknadId: String? = null): String {
        if (soknadId == "7d947d8b-3d85-4411-979f-3eb9f13a6c0e") {
            return "4474"
        }

        val pdlRes = pdlClient.hentGeografiskTilknytning(fnr)
        val pdlGT = pdlRes.hentGeografiskTilknytning?.finnGT()
        val diskresjonskode = pdlRes.hentPerson?.getDiskresjonskode()

        if (soknadstype == Soknadstype.REISETILSKUDD) {
            if (diskresjonskode == DISKRESJONSKODE_KODE6) {
                return NAV_VIKAFOSSEN
            }
            return "4488"
        }

        return arbeidsfordelingConsumer.finnBehandlendeEnhet(GeografiskTilknytning(pdlGT, diskresjonskode), soknadstype)
    }
}

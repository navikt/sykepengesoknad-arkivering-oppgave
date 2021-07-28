package no.nav.syfo.service

import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.pdl.finnGT
import no.nav.syfo.client.pdl.getDiskresjonskode
import no.nav.syfo.domain.dto.Soknadstype
import org.springframework.stereotype.Component

const val NAV_VIKAFOSSEN = "2103"
const val DISKRESJONSKODE_KODE6 = "SPSF"
const val DISKRESJONSKODE_KODE7 = "SPFO"

@Component
class BehandlendeEnhetService(
    private val pdlClient: PdlClient,
) {
    // TODO: Fjern egne regler, oppdater norg2 arbeidsfordelinger regelsettet
    fun hentBehandlendeEnhet(fnr: String, soknadstype: Soknadstype): String? {
        val pdlRes = pdlClient.hentGeografiskTilknytning(fnr)
        val pdlGT = pdlRes.hentGeografiskTilknytning?.finnGT()
        val diskresjonskode = pdlRes.hentPerson?.getDiskresjonskode()

        if (soknadstype == Soknadstype.REISETILSKUDD || soknadstype == Soknadstype.GRADERT_REISETILSKUDD) {
            if (diskresjonskode == DISKRESJONSKODE_KODE6) {
                return NAV_VIKAFOSSEN
            }
            return "4488"
        }

        if (pdlGT == "NOR") {
            return "4474"
        }

        return null
    }
}

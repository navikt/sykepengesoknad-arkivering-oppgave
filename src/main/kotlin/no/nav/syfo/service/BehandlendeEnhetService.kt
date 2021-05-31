package no.nav.syfo.service

import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.client.pdl.finnGT
import no.nav.syfo.client.pdl.getDiskresjonskode
import no.nav.syfo.consumer.ws.ArbeidsfordelingConsumer
import no.nav.syfo.consumer.ws.GeografiskTilknytning
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

        if (soknadstype == Soknadstype.REISETILSKUDD) {
            if (geografiskTilknytning.diskresjonskode == DISKRESJONSKODE_KODE6) {
                return NAV_VIKAFOSSEN
            }
            return "4488"
        }

        val arbeidsfordeling = arbeidsfordelingConsumer.finnBehandlendeEnhet(geografiskTilknytning, soknadstype)
        val pdlArbeidsfordeling = arbeidsfordelingConsumer.finnBehandlendeEnhet(GeografiskTilknytning(pdlGT, diskresjonskode), soknadstype)

        if (arbeidsfordeling != pdlArbeidsfordeling) {
            log.warn("arbeidsfordeling: $arbeidsfordeling, pdl: $pdlArbeidsfordeling")
            if (geografiskTilknytning.diskresjonskode == DISKRESJONSKODE_KODE6 &&
                diskresjonskode != DISKRESJONSKODE_KODE6
            ) {
                log.warn("GT case 1, pdl: $diskresjonskode")
            } else if (geografiskTilknytning.diskresjonskode == DISKRESJONSKODE_KODE7 &&
                diskresjonskode != DISKRESJONSKODE_KODE7
            ) {
                log.warn("GT case 2, pdl: $diskresjonskode")
            } else if (geografiskTilknytning.diskresjonskode != diskresjonskode) {
                log.warn("Disk: ${geografiskTilknytning.diskresjonskode}, pdl: $diskresjonskode")
            }
        }

        return arbeidsfordeling
    }
}

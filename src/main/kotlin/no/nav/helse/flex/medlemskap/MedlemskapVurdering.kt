package no.nav.helse.flex.medlemskap

import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.logger
import org.springframework.stereotype.Component

@Component
class MedlemskapVurdering(
    private val medlemskapVurderingRepository: MedlemskapVurderingRepository,
    private val lovMeClient: LovMeClient,
) {
    private val log = logger()

    fun oppdaterInngåendeMedlemskapVurdering(sykepengesoknad: Sykepengesoknad) {
        if (!sykepengesoknad.skalHaMedlemskapVurering()) {
            return
        }
        if (medlemskapVurderingRepository.findBySykepengesoknadId(sykepengesoknad.id) != null) {
            return
        }

        val statusFraSoknad = sykepengesoknad.medlemskapVurdering
        if (statusFraSoknad != null) {
            log.info("Lagrer inngående medlemskap vurdering $statusFraSoknad for søknad ${sykepengesoknad.id}")
            medlemskapVurderingRepository.save(
                MedlemskapVurderingDbRecord(
                    fnr = sykepengesoknad.fnr,
                    sykepengesoknadId = sykepengesoknad.id,
                    fom = sykepengesoknad.fom!!,
                    tom = sykepengesoknad.tom!!,
                    inngaendeVurdering = statusFraSoknad,
                ),
            )
            return
        }
    }

    fun hentEndeligMedlemskapVurdering(sykepengesoknad: Sykepengesoknad): String? {
        if (!sykepengesoknad.skalHaMedlemskapVurering()) {
            return null
        }

        val tidligereVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(sykepengesoknad.id)
        if (tidligereVurdering == null) {
            log.info("Søknad ${sykepengesoknad.id} har ikke inngående vurdering, så gjør ikke endelig vurdering.")
            return null
        }

        // Returnerer endelig vurdering hvis den allerede finnes.
        tidligereVurdering.endeligVurdering?.let {
            log.info("Søknad ${sykepengesoknad.id} har allerede endelig vurdering: $it.")
            return it
        }

        // Gjør at vi lagrer "null" som endelig vurdering hvis kallet til LovMe feiler.
        val endeligVurdering = lovMeClient.hentEndeligMedlemskapVurdering(sykepengesoknad) ?: return null

        val oppdatertVurdering =
            tidligereVurdering.copy(
                vurderingId = endeligVurdering.vurdering_id,
                endeligVurdering = endeligVurdering.status.name,
            )

        medlemskapVurderingRepository.save(oppdatertVurdering)
        return oppdatertVurdering.endeligVurdering
    }

    private fun Sykepengesoknad.skalHaMedlemskapVurering() =
        soknadstype in
            listOf(
                Soknadstype.ARBEIDSTAKERE,
                Soknadstype.GRADERT_REISETILSKUDD,
            )
}

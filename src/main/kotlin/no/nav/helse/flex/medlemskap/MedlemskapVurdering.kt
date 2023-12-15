package no.nav.helse.flex.medlemskap

import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.logger
import org.springframework.stereotype.Component

@Component
class MedlemskapVurdering(
    private val medlemskapVurderingRepository: MedlemskapVurderingRepository,
    private val lovMeClient: LovMeClient,
    private val medlemskapToggle: MedlemskapToggle
) {
    private val log = logger()

    fun oppdaterInngåendeMedlemskapVurdering(sykepengesoknad: Sykepengesoknad) {
        if (!sykepengesoknad.skalGjøreMedlemskapVurering()) {
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
                    inngaendeVurdering = statusFraSoknad
                )
            )
            return
        }
    }

    fun hentEndeligMedlemskapVurdering(sykepengesoknad: Sykepengesoknad): String? {
        if (!sykepengesoknad.skalGjøreMedlemskapVurering()) {
            return null
        }

        val tidligereVurdering = medlemskapVurderingRepository.findBySykepengesoknadId(sykepengesoknad.id)
        if (tidligereVurdering == null) {
            log.info("Søknad ${sykepengesoknad.id} er ikke medlemskap vurdert")
            return null
        }
        if (tidligereVurdering.endeligVurdering != null) {
            log.info("Søknad ${sykepengesoknad.id} er allerede medlemskap vurdert til ${tidligereVurdering.endeligVurdering}")

            if (medlemskapToggle.medlemskapToggleForBruker(sykepengesoknad.fnr)) {
                return tidligereVurdering.endeligVurdering
            }

            return null
        }

        val endeligVurdering = lovMeClient.hentEndeligMedlemskapVurdering(sykepengesoknad) ?: return null
        val oppdatertVurdering = tidligereVurdering.copy(
            vurderingId = endeligVurdering.vurdering_id,
            endeligVurdering = endeligVurdering.status.name
        )
        medlemskapVurderingRepository.save(oppdatertVurdering)

        if (medlemskapToggle.medlemskapToggleForBruker(sykepengesoknad.fnr)) {
            return oppdatertVurdering.endeligVurdering
        }

        return null
    }

    private fun Sykepengesoknad.skalGjøreMedlemskapVurering() = soknadstype in listOf(Soknadstype.ARBEIDSTAKERE, Soknadstype.GRADERT_REISETILSKUDD)
}

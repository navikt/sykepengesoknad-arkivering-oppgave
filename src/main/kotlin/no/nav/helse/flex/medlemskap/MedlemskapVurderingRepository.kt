package no.nav.helse.flex.medlemskap

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface MedlemskapVurderingRepository : CrudRepository<MedlemskapVurderingDbRecord, String> {
    fun findBySykepengesoknadId(sykepengesoknadId: String): MedlemskapVurderingDbRecord?
}

@Table("medlemskap_vurdering")
data class MedlemskapVurderingDbRecord(
    @Id
    val id: String? = null,
    val fnr: String,
    val sykepengesoknadId: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val inngaendeVurdering: String,
    val vurderingId: String? = null,
    val endeligVurdering: String? = null,
)

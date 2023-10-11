package no.nav.helse.flex.medlemskap

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface MedlemskapVurderingRepository : CrudRepository<MedlemskapVurderingDbRecord, String> {
    fun findBySykepengesoknadId(sykepengesoknadId: String): MedlemskapVurderingDbRecord?

    @Query(
        """
        SELECT v1.* FROM medlemskap_vurdering AS v1
        JOIN (
            SELECT MAX(tom) AS maxTom
            FROM medlemskap_vurdering
            WHERE fnr = :fnr
            GROUP BY fnr
        ) AS v2 ON v1.tom = v2.maxTom
        WHERE v1.fnr = :fnr
    """
    )
    fun tidligereMedlemskapVurdering(fnr: String): MedlemskapVurderingDbRecord?
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
    val endeligVurdering: String? = null
)

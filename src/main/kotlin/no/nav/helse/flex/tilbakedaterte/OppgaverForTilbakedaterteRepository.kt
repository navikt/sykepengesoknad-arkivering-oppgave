package no.nav.helse.flex.tilbakedaterte

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface OppgaverForTilbakedaterteRepository : CrudRepository<OppgaverForTilbakedaterteDbRecord, String> {
    fun findBySykmeldingUuid(sykmeldingUuid: String): List<OppgaverForTilbakedaterteDbRecord>
}

@Table("oppgaver_for_tilbakedaterte")
data class OppgaverForTilbakedaterteDbRecord(
    @Id
    val id: String? = null,
    val sykepengesoknadUuid: String,
    val sykmeldingUuid: String,
    val oppgaveId: String,
    val status: OppgaverForTilbakedaterteStatus,
    val opprettet: Instant,
    val oppdatert: Instant?,
)

enum class OppgaverForTilbakedaterteStatus {
    OPPRETTET,
    OPPDATERT,
    OPPGAVE_ALLEREDE_FERDIGSTILT,
    IKKE_GODKJENT,
}

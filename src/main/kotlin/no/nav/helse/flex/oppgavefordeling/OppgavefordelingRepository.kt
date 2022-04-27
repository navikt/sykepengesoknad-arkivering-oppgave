package no.nav.helse.flex.oppgavefordeling

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface OppgavefordelingRepository : CrudRepository<OppgavefordelingDbRecord, String> {
    fun findBySykepengesoknadId(sykepengesoknadId: String): OppgavefordelingDbRecord?

    @Modifying
    @Query("""INSERT INTO oppgavefordeling(sykepengesoknad_id, status) VALUES (:sykepengesoknadId, :status)""")
    fun insert(sykepengesoknadId: String, status: OppgavefordelingStatus)

    @Modifying
    @Query("""UPDATE oppgavefordeling SET avstemt = TRUE, sendt_nav = :sendtNav WHERE sykepengesoknad_id = :sykepengesoknadId""")
    fun settTilAvstemt(sykepengesoknadId: String, sendtNav: Instant): Boolean

    @Modifying
    @Query("""UPDATE oppgavefordeling SET korrigert_av = :korrigertAv WHERE sykepengesoknad_id = :sykepengesoknadId""")
    fun settkorrigertAv(korrigertAv: String, sykepengesoknadId: String): Boolean

    @Modifying
    @Query("""UPDATE oppgavefordeling SET sendt_arbeidsgiver = :sendtArbeidsgiver WHERE sykepengesoknad_id = :sykepengesoknadId""")
    fun settSendtArbeidsgiver(sykepengesoknadId: String, sendtArbeidsgiver: Instant): Boolean
}

@Table("oppgavefordeling")
data class OppgavefordelingDbRecord(
    @Id
    val sykepengesoknadId: String,
    val status: OppgavefordelingStatus,
    val timeout: Instant? = null,
    val avstemt: Boolean = false,
    val sendtNav: Instant? = null,
    val sendtArbeidsgiver: Instant? = null,
    val korrigertAv: String? = null,
)

enum class OppgavefordelingStatus {
    LagOppgave, LagOppgaveForSpeilsaksbehandlere
}

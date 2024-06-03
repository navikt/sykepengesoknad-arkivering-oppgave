package no.nav.helse.flex.egenemeldinggate

import no.nav.helse.flex.logger
import no.nav.helse.flex.repository.*
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.*

@Component
class FinnBerorteOppgaverTx(
    private val spreOppgaveRepository: SpreOppgaveRepository,
    private val innsendingRepository: InnsendingRepository,
) {
    private val log = logger()

    @Transactional(propagation = Propagation.REQUIRED)
    fun behandleOppgaverTransactional(
        spreOppgaveDbRecord: SpreOppgaveDbRecord,
        innsending: InnsendingDbRecord,
    ) {
        log.info(
            "Fjerner FEILREGISTRERT oppgave id ${innsending.oppgaveId} fra innsending ${innsending.id} " +
                "med sykepengesoknadId ${spreOppgaveDbRecord.sykepengesoknadId}",
        )
        innsendingRepository.save(innsending.copy(oppgaveId = null))

        log.info(
            "Oppdaterer status til UTSETT for oppgavestyring ${spreOppgaveDbRecord.id} med " +
                "sykepengesoknadId ${spreOppgaveDbRecord.sykepengesoknadId}",
        )
        spreOppgaveRepository.save(
            spreOppgaveDbRecord.copy(
                status = OppgaveStatus.Utsett,
                timeout =
                    OffsetDateTime.of(2024, 6, 6, 3, 0, 0, 0, ZoneOffset.UTC)
                        .toInstant(),
                modifisert = Instant.now(),
            ),
        )
    }
}

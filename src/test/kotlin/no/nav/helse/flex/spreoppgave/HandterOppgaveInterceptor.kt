package no.nav.helse.flex.spreoppgave

import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveDbRecord
import no.nav.helse.flex.repository.SpreOppgaveRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Kun-for-test-interceptor brukt til å simulere race conditions under opprettelsen av SpreOppgave-poster.
 *
 * Interceptoren gjør det mulig å teste av håndtering av DuplicateKeyException ved å manuelt
 * sette inn en record i databasen etter at tjenesten har sjekket om en record allerede finnes.
 * Dette fremtvinger en kollisjon som etterligner at en oppgave kommer samtidig fra Søknad og
 * Bømlo, og gjør det mulig å verifisere at Kafka-lyttere
 * korrekt fanger opp Exception tvinger frem en retry med acknowledgment.nack().
 */
@Component
@Primary
class HandterOppgaveInterceptor(
    private val handterOppgave: HandterOppgave,
    private val spreOppgaveRepository: SpreOppgaveRepository,
) : HandterOppgaveInterface {
    companion object {
        val raceConditionUUID: UUID = UUID.randomUUID()
        val raceConditionTimeout: Instant = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.SECONDS)
        var kallTilHåndterOppgaveFraBømlo = 0
        var kallTilHåndterOppgaveFraSøknad = 0
    }

    override fun håndterOppgaveFraBømlo(
        eksisterendeOppgave: SpreOppgaveDbRecord?,
        oppgave: OppgaveDTO,
    ) {
        kallTilHåndterOppgaveFraBømlo += 1
        if (eksisterendeOppgave == null && oppgave.dokumentId == raceConditionUUID) {
            insertSpreOppgaveMellomHentingAvEksisterendeOgLagring()
            spreOppgaveRepository.updateAvstemtBySykepengesoknadId(oppgave.dokumentId.toString())
        }

        handterOppgave.håndterOppgaveFraBømlo(eksisterendeOppgave, oppgave)
    }

    override fun håndterOppgaveFraSøknad(
        eksisterendeOppgave: SpreOppgaveDbRecord?,
        oppgave: OppgaveDTO,
    ) {
        kallTilHåndterOppgaveFraSøknad += 1
        if (eksisterendeOppgave == null && oppgave.dokumentId == raceConditionUUID) {
            insertSpreOppgaveMellomHentingAvEksisterendeOgLagring()
        }

        handterOppgave.håndterOppgaveFraSøknad(eksisterendeOppgave, oppgave)
    }

    private fun insertSpreOppgaveMellomHentingAvEksisterendeOgLagring() {
        val tidspunkt = Instant.now()

        spreOppgaveRepository.save(
            SpreOppgaveDbRecord(
                sykepengesoknadId = raceConditionUUID.toString(),
                timeout = raceConditionTimeout,
                status = OppgaveStatus.Utsett,
                opprettet = tidspunkt,
                modifisert = tidspunkt,
            ),
        )
    }
}

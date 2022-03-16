package no.nav.helse.flex.arkivering

import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.oppgave.BeskrivelseServiceTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class SkapJournalpostRequestKtTest {

    @Test
    fun `filnavn inneholder innsendt dato`() {
        val sykepengesoknad = objectMapper.readValue(
            BeskrivelseServiceTest::class.java.getResource("/soknadUtland.json"),
            Sykepengesoknad::class.java
        )

        val soknad = Soknad.lagSoknad(sykepengesoknad, "fnr", "navn")

        val journalpostRequest = skapJournalpostRequest("PDF".toByteArray(), soknad)
        journalpostRequest.dokumenter[0].dokumentvarianter[0].filnavn `should be equal to` "soknad-16.11.2019"
    }
}

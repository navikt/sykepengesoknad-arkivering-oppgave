package no.nav.helse.flex

import no.nav.helse.FellesTestoppsett
import no.nav.helse.flex.repository.InnsendingDbRecord
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.service.PatchDataUtenBehandletTidspunkt
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate
import java.time.ZoneOffset

@DirtiesContext
class PatchDataTest : FellesTestoppsett() {

    @Autowired
    private lateinit var innsendingRepository: InnsendingRepository

    @Autowired
    private lateinit var patchDataUtenBehandletTidspunkt: PatchDataUtenBehandletTidspunkt

    @Test
    fun `Behandling av søknad feiler og rebehandles`() {

        val innsending = innsendingRepository.save(
            InnsendingDbRecord(
                id = null,
                sykepengesoknadId = "049c350f-9117-3217-b13c-e7c25fcffeee",

            )
        )

        patchDataUtenBehandletTidspunkt.startJob()

        val oppdatert = innsendingRepository.findByIdOrNull(innsending.id!!)!!
        oppdatert.behandlet!!.atOffset(ZoneOffset.UTC).toLocalDate() `should be equal to` LocalDate.of(2022, 2, 9)
    }
}

package no.nav.helse.flex.oppgavefordeling

import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.any
import no.nav.helse.flex.client.SyfosoknadClient
import no.nav.helse.flex.mockSykepengesoknadDTO
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.InnsendingDbRecord
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.OppgaveRequest
import no.nav.helse.flex.service.OppgaveResponse
import no.nav.helse.flex.service.OppgaveService
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.time.format.DateTimeFormatter
import java.util.UUID

@DirtiesContext
class OpprettOppgaverSomManglerTest : FellesTestoppsett() {

    @Autowired
    lateinit var oppgavefordelingRepository: OppgavefordelingRepository

    @Autowired
    lateinit var opprettOppgaverSomMangler: OpprettOppgaverSomMangler

    @Autowired
    lateinit var innsendingRepository: InnsendingRepository

    @MockBean
    lateinit var syfosoknadClient: SyfosoknadClient

    @MockBean
    lateinit var oppgaveService: OppgaveService

    @BeforeAll
    fun beforeAll() {
        oppgavefordelingRepository.finnOppgaverSomMangler().size shouldBeEqualTo 0
    }

    @Test
    fun `Oppgavefordeling som ikke er avstemt hentes ikke ut`() {
        val id = UUID.randomUUID().toString()

        oppgavefordelingRepository.insert(
            sykepengesoknadId = id,
            status = OppgavefordelingStatus.LagOppgave,
        )

        opprettOppgaverSomMangler.finnUferdigeOppgavefordelinger()

        verify(oppgaveService, never()).opprettOppgave(any())

        oppgavefordelingRepository.finnOppgaverSomMangler().size shouldBeEqualTo 0
        oppgavefordelingRepository.findBySykepengesoknadId(id)?.ferdig shouldBeEqualTo false
    }

    @Test
    fun `Oppgavefordeling som allerede har gosys oppgave hentes ikke ut`() {
        val id = UUID.randomUUID().toString()
        val oppgaveId = "10"

        prepMedSoknad(id)

        oppgavefordelingRepository.settTilAvstemt(id)

        val innsendingId = innsendingRepository.findBySykepengesoknadId(id)!!.id!!
        innsendingRepository.updateOppgaveId(innsendingId, oppgaveId)
        innsendingRepository.updateBehandlet(innsendingId)

        opprettOppgaverSomMangler.finnUferdigeOppgavefordelinger()

        verify(oppgaveService, never()).opprettOppgave(any())

        innsendingRepository.findBySykepengesoknadId(id)!!.oppgaveId shouldBeEqualTo oppgaveId
        oppgavefordelingRepository.findBySykepengesoknadId(id)!!.ferdig shouldBeEqualTo false
    }

    @Test
    fun `Oppgavefordeling som er avstemt og mangler oppgave blir opprettet`() {
        val id = UUID.randomUUID().toString()
        prepMedSoknad(id)
        oppgavefordelingRepository.settTilAvstemt(id)

        opprettOppgaverSomMangler.finnUferdigeOppgavefordelinger()

        val captor: KArgumentCaptor<OppgaveRequest> = argumentCaptor()
        verify(oppgaveService, times(1)).opprettOppgave(captor.capture())

        val arg = captor.firstValue
        arg.tema shouldBeEqualTo "SYK"
        arg.oppgavetype shouldBeEqualTo "SOK"
        arg.aktivDato shouldBeEqualTo mockSykepengesoknadDTO.sendtNav!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        arg.behandlingstema shouldBeEqualTo "TODO"
        arg.behandlingstype shouldBeEqualTo null

        innsendingRepository.findBySykepengesoknadId(id)!!.oppgaveId shouldNotBeEqualTo null
        oppgavefordelingRepository.findBySykepengesoknadId(id)!!.ferdig shouldBeEqualTo true
    }

    fun prepMedSoknad(id: String) {
        val soknad = mockSykepengesoknadDTO.copy(id = id)

        innsendingRepository.save(
            InnsendingDbRecord(
                sykepengesoknadId = soknad.id,
                journalpostId = "20",
                oppgaveId = null,
            )
        )

        oppgavefordelingRepository.insert(
            sykepengesoknadId = soknad.id,
            status = OppgavefordelingStatus.LagOppgave,
        )

        whenever(syfosoknadClient.hentSoknad(any())).thenReturn(
            objectMapper.readValue(
                soknad.serialisertTilString(),
                SykepengesoknadDTO::class.java
            )
        )

        whenever(oppgaveService.opprettOppgave(any()))
            .thenReturn(OppgaveResponse(30))
    }
}

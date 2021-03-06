package no.nav.helse.flex.spreoppgave

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.any
import no.nav.helse.flex.arkivering.Arkivaren
import no.nav.helse.flex.client.SykepengesoknadBackendClient
import no.nav.helse.flex.client.pdl.PdlClient
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.kafka.consumer.SPREOPPGAVER_TOPIC
import no.nav.helse.flex.kafka.consumer.SYKEPENGESOKNAD_TOPIC
import no.nav.helse.flex.mockSykepengesoknadDTO
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.IdentService
import no.nav.helse.flex.service.OppgaveResponse
import no.nav.helse.flex.service.OppgaveService
import no.nav.helse.flex.spreoppgave.HandterOppgaveInterceptor.Companion.raceConditionTimeout
import no.nav.helse.flex.spreoppgave.HandterOppgaveInterceptor.Companion.raceConditionUUID
import no.nav.helse.flex.util.tilOsloZone
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@DirtiesContext
class SpreOppgaveRaceConditionTest : FellesTestoppsett() {
    @Autowired
    lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @Autowired
    private lateinit var aivenKafkaProducer: KafkaProducer<String, String>

    @SpyBean
    private lateinit var handterOppagave: HandterOppave

    @MockBean
    lateinit var oppgaveService: OppgaveService

    @MockBean
    lateinit var arkivaren: Arkivaren

    @MockBean
    lateinit var identService: IdentService

    @MockBean
    lateinit var pdlClient: PdlClient

    @MockBean
    lateinit var sykepengesoknadBackendClient: SykepengesoknadBackendClient

    private val s??knad = mockSykepengesoknadDTO.copy(
        id = raceConditionUUID.toString(),
        sendtNav = LocalDateTime.now(),
    )

    @BeforeEach
    fun setup() {
        whenever(identService.hentAktorIdForFnr(any())).thenReturn(s??knad.fnr)
        whenever(identService.hentFnrForAktorId(any())).thenReturn(s??knad.fnr)
        whenever(pdlClient.hentFormattertNavn(any())).thenReturn("Kalle Klovn")
        whenever(arkivaren.opprettJournalpost(any())).thenReturn("jpost1234")
        whenever(oppgaveService.opprettOppgave(any())).thenReturn(OppgaveResponse(123))
        whenever(sykepengesoknadBackendClient.hentSoknad(any())).thenReturn(s??knad)
    }

    @AfterEach
    fun `slett spre oppgaver`() {
        jdbcTemplate.update("DELETE FROM oppgavestyring")
    }

    @Test
    fun `H??ndtering av DuplicateKeyException n??r spre oppgave opprettes i fra s??knad og b??mlo samtidig`() {
        val timeoutFraB??mlo = LocalDateTime.now().plusMinutes(29).truncatedTo(ChronoUnit.SECONDS)

        aivenKafkaProducer.send(
            ProducerRecord(
                SPREOPPGAVER_TOPIC,
                raceConditionUUID.serialisertTilString(),
                OppgaveDTO(
                    dokumentType = DokumentTypeDTO.S??knad,
                    oppdateringstype = OppdateringstypeDTO.Utsett,
                    dokumentId = raceConditionUUID,
                    timeout = timeoutFraB??mlo,
                ).serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10)).until {
            spreOppgaveRepository.findBySykepengesoknadId(
                raceConditionUUID.toString()
            )?.timeout == timeoutFraB??mlo.tilOsloZone().toInstant()
        }

        // Skal kalles 2 ganger, f??rste gang gir DuplicateKeyException, andre gang g??r ok
        verify(handterOppagave, times(2)).h??ndterOppgaveFraB??mlo(any(), any())

        spreOppgaveRepository.findBySykepengesoknadId(
            raceConditionUUID.toString()
        )!!.timeout shouldBeEqualTo timeoutFraB??mlo.tilOsloZone().toInstant()
    }

    @Test
    fun `H??ndteres ogs?? n??r s??knaden kaster DuplicateKeyException`() {
        aivenKafkaProducer.send(
            ProducerRecord(
                SYKEPENGESOKNAD_TOPIC,
                s??knad.id,
                s??knad.serialisertTilString()
            )
        )

        await().atMost(Duration.ofSeconds(10)).until {
            spreOppgaveRepository.findBySykepengesoknadId(
                raceConditionUUID.toString()
            )?.avstemt == true
        }

        // Skal kalles 2 ganger, f??rste gang gir DuplicateKeyException, andre gang g??r ok
        verify(handterOppagave, times(2)).h??ndterOppgaveFraS??knad(any(), any())

        spreOppgaveRepository.findBySykepengesoknadId(
            raceConditionUUID.toString()
        )!!.timeout shouldBeEqualTo raceConditionTimeout
    }
}

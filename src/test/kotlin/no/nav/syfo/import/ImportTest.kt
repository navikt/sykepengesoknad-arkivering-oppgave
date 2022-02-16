package no.nav.syfo.import

import no.nav.syfo.FellesTestoppsett
import no.nav.syfo.repository.InnsendingRepository
import no.nav.syfo.repository.OppgaveStatus
import no.nav.syfo.repository.SpreOppgaveRepository
import no.nav.syfo.serialisertTilString
import no.nav.syfo.skapConsumerRecord
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.annotation.DirtiesContext
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

@DirtiesContext
class ImportTest : FellesTestoppsett() {

    @MockBean
    lateinit var acknowledgment: Acknowledgment

    @Autowired
    lateinit var innsendingImportListener: InnsendingImportListener

    @Autowired
    lateinit var oppgavestyringImportListener: OppgavestyringImportListener

    @Autowired
    lateinit var innsendingRepository: InnsendingRepository

    @Autowired
    lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @BeforeEach
    fun setup() {
        innsendingRepository.deleteAll()
        spreOppgaveRepository.deleteAll()
    }

    @Test
    fun `Mottar innsending fra kafka`() {
        innsendingRepository.findAll().iterator().asSequence().toList().shouldBeEmpty()

        val innsending = InnsendingKafkaDto(
            sykepengesoknadId = "1234",
            journalpostId = "123",
            oppgaveId = "12fq43",
            behandlet = OffsetDateTime.now()
        )
        leggInnsendingPaKafka(innsending)

        val records = innsendingRepository.findAll().iterator().asSequence().toList()
        records.shouldHaveSize(1)
        records.first().sykepengesoknadId `should be equal to` innsending.sykepengesoknadId
        records.first().journalpostId `should be equal to` innsending.journalpostId
        records.first().oppgaveId `should be equal to` innsending.oppgaveId
        records.first().behandlet!!.truncatedTo(ChronoUnit.SECONDS) `should be equal to` innsending.behandlet!!.toInstant()
            .truncatedTo(ChronoUnit.SECONDS)
    }

    @Test
    fun `Mottar oppgave fra kafka`() {
        spreOppgaveRepository.findAll().iterator().asSequence().toList().shouldBeEmpty()

        val oppgave = OppgavestyringKafkaDto(
            sykepengesoknadId = "1234",
            status = OppgaveStatus.Opprettet,
            avstemt = true,
            timeout = null,
            opprettet = OffsetDateTime.now(),
            modifisert = OffsetDateTime.now(),
        )
        leggOppgavePaKafka(oppgave)

        val records = spreOppgaveRepository.findAll().iterator().asSequence().toList()
        records.shouldHaveSize(1)
        records.first().sykepengesoknadId `should be equal to` oppgave.sykepengesoknadId
        records.first().status `should be equal to` oppgave.status
        records.first().avstemt `should be equal to` oppgave.avstemt
        records.first().timeout.`should be null`()
    }

    private fun leggInnsendingPaKafka(innsending: InnsendingKafkaDto) =
        innsendingImportListener.listen(
            listOf(skapConsumerRecord("key", innsending.serialisertTilString())),
            acknowledgment
        )

    private fun leggOppgavePaKafka(oppgave: OppgavestyringKafkaDto) =
        oppgavestyringImportListener.listen(
            listOf(skapConsumerRecord("key", oppgave.serialisertTilString())),
            acknowledgment
        )
}

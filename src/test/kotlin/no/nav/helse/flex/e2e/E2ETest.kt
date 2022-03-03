package no.nav.helse.flex.e2e

import com.nhaarman.mockitokotlin2.whenever
import no.nav.helse.FellesTestoppsett
import no.nav.helse.flex.`should be equal to ignoring nano and zone`
import no.nav.helse.flex.any
import no.nav.helse.flex.client.SyfosoknadClient
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.kafka.consumer.AivenSoknadSendtListener
import no.nav.helse.flex.kafka.consumer.AivenSpreOppgaverListener
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.repository.InnsendingDbRecord
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.BehandleVedTimeoutService
import no.nav.helse.flex.service.SaksbehandlingsService
import no.nav.helse.flex.skapConsumerRecord
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvartypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.helse.flex.util.tilOsloZone
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@DirtiesContext
class E2ETest : FellesTestoppsett() {

    companion object {
        val aktørId = "aktørId"
        val fnr = "fnr"
        val omFireTimer = LocalDateTime.now().plusHours(4)
    }

    @MockBean
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @MockBean
    lateinit var acknowledgment: Acknowledgment

    @MockBean
    lateinit var syfosoknadClient: SyfosoknadClient

    @Autowired
    lateinit var aivenSoknadSendtListener: AivenSoknadSendtListener

    @Autowired
    lateinit var aivenSpreOppgaverListener: AivenSpreOppgaverListener

    @Autowired
    lateinit var spreOppgaveRepository: SpreOppgaveRepository

    @Autowired
    lateinit var behandleVedTimeoutService: BehandleVedTimeoutService

    @BeforeEach
    fun setup() {
        whenever(saksbehandlingsService.finnEksisterendeInnsending(any())).thenAnswer {
            InnsendingDbRecord(
                id = "iid",
                sykepengesoknadId = it.arguments[0].toString(),
                journalpostId = "journalpost"
            )
        }
        whenever(syfosoknadClient.hentSoknad(any())).thenReturn(
            objectMapper.readValue(
                søknad().serialisertTilString(),
                SykepengesoknadDTO::class.java
            )
        )
    }

    @Test
    fun `bømlo sier opprett før vi behandler søknad`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, søknadsId, null))

        val oppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Opprett).isEqualTo(oppgave.status)
        assertThat(oppgave.timeout).isNull()
        assertThat(oppgave.avstemt).isFalse

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Opprett).isEqualTo(oppgaveFraAiven.status)
        assertThat(oppgaveFraAiven.timeout).isNull()
        assertThat(oppgaveFraAiven.avstemt).isFalse
    }

    @Test
    fun `bømlo sier utsett før vi behandler søknad`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))

        val oppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Utsett).isEqualTo(oppgave.status)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgave.timeout
        assertThat(oppgave.avstemt).isFalse

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Utsett).isEqualTo(oppgaveFraAiven.status)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgaveFraAiven.timeout
        assertThat(oppgaveFraAiven.avstemt).isFalse
    }

    @Test
    fun `bømlo sier ferdigbehandlet før vi behandler søknad`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Ferdigbehandlet, søknadsId, null))

        val oppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.IkkeOpprett).isEqualTo(oppgave.status)
        assertThat(oppgave.timeout).isNull()
        assertThat(oppgave.avstemt).isFalse

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.IkkeOpprett).isEqualTo(oppgaveFraAiven.status)
        assertThat(oppgaveFraAiven.timeout).isNull()
        assertThat(oppgaveFraAiven.avstemt).isFalse
    }

    @Test
    fun `vi behandler søknad så kommer utsett fra bømlo`() {
        val søknadsId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(søknadsId))
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))

        val oppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Utsett).isEqualTo(oppgave.status)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgave.timeout
        assertThat(oppgave.avstemt).isTrue

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Utsett).isEqualTo(oppgaveFraAiven.status)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgaveFraAiven.timeout
        assertThat(oppgaveFraAiven.avstemt).isTrue
    }

    @Test
    fun `vi behandler søknad så kommer opprett fra bømlo`() {
        val søknadsId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(søknadsId))
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, søknadsId))

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Opprett).isEqualTo(oppgaveFraAiven.status)
        assertThat(oppgaveFraAiven.timeout).isNull()
        assertThat(oppgaveFraAiven.avstemt).isTrue
    }

    @Test
    fun `vi behandler søknad så kommer ferdigbehandlet fra bømlo`() {
        val søknadsId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(søknadsId))
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Ferdigbehandlet, søknadsId))

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.IkkeOpprett).isEqualTo(oppgaveFraAiven.status)
        assertThat(oppgaveFraAiven.timeout).isNull()
        assertThat(oppgaveFraAiven.avstemt).isTrue
    }

    @Test
    fun `bømlo sier utsett så behandler vi søknaden og utsetter oppgave`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))
        leggSøknadPåKafka(søknad(søknadsId))

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Utsett).isEqualTo(oppgaveFraAiven.status)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgaveFraAiven.timeout!!
        assertThat(oppgaveFraAiven.avstemt).isTrue
    }


    @Test
    fun `oppgaven timer ut og vi oppretter oppgave`() {
        val søknadsId = UUID.randomUUID()
        val timeout = LocalDateTime.now().minusHours(1)

        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, timeout))
        leggSøknadPåKafka(søknad(søknadsId))

        behandleVedTimeoutService.behandleTimeout()
       // leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.OpprettetTimeout).isEqualTo(oppgaveFraAiven.status)
        assertThat(oppgaveFraAiven.timeout).isNull()
    }

    @Test
    fun `bømlo sier Opprett og oppgave får Opprettet status`() {
        val søknadsId = UUID.randomUUID()

        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, søknadsId, omFireTimer))
        leggSøknadPåKafka(søknad(søknadsId))

        behandleVedTimeoutService.behandleTimeout()

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))

        assertThat(oppgaveFraAiven.status).isEqualTo(OppgaveStatus.Opprettet)
        assertThat(oppgaveFraAiven.timeout).isNull()
    }

    @Test
    fun `bømlo sier OpprettSpeilRelatert og oppgave får OpprettetSpeilRelatert status`() {
        val søknadsId = UUID.randomUUID()
        val timeout = LocalDateTime.now().minusHours(1)

        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.OpprettSpeilRelatert, søknadsId, omFireTimer))
        leggSøknadPåKafka(søknad(søknadsId))

        behandleVedTimeoutService.behandleTimeout()
        // leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))

        assertThat(oppgaveFraAiven.status).isEqualTo(OppgaveStatus.OpprettetSpeilRelatert)
        assertThat(oppgaveFraAiven.timeout).isNull()
    }

    @Test
    fun `bømlo sier utsett etter oppgaven er opprettet`() {
        val søknadsId = UUID.randomUUID()
        val førsteTimeout = LocalDateTime.now().minusHours(1)
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, førsteTimeout))
        leggSøknadPåKafka(søknad(søknadsId))

        behandleVedTimeoutService.behandleTimeout()
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.OpprettetTimeout).isEqualTo(oppgaveFraAiven.status)
        assertThat(oppgaveFraAiven.timeout).isNull()
    }

    @Test
    fun `bømlo sier utsett etter oppgaven er ferdigbehandlet`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Ferdigbehandlet, søknadsId))
        leggSøknadPåKafka(søknad(søknadsId))
        leggOppgavePåAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, søknadsId, omFireTimer))

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.IkkeOpprett).isEqualTo(oppgaveFraAiven.status)
        assertThat(oppgaveFraAiven.timeout).isNull()
    }

    @Test
    fun `bømlo sier utsett så behandler vi søknaden og ikke oppretter oppgave`() {
        val søknadsId = UUID.randomUUID()
        leggOppgavePåAivenKafka(
            OppgaveDTO(
                dokumentType = DokumentTypeDTO.Søknad,
                oppdateringstype = OppdateringstypeDTO.Utsett,
                dokumentId = søknadsId,
                timeout = LocalDateTime.now().plusHours(1)
            )
        )
        leggSøknadPåKafka(søknad(søknadsId = søknadsId, sendtNav = null, sendtArbeidsgiver = LocalDateTime.now()))

        val oppgaveFørJob = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Utsett).isEqualTo(oppgaveFørJob.status)

        assertThat(oppgaveFørJob.avstemt).isFalse

        behandleVedTimeoutService.behandleTimeout()

        val oppgaveEtterJob = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Utsett).isEqualTo(oppgaveEtterJob.status)
        assertThat(oppgaveEtterJob.avstemt).isFalse
    }

    @Test
    fun `vi behandler søknaden uten oppgave så sender bømlo utsett`() {
        val søknadsId = UUID.randomUUID()
        leggSøknadPåKafka(søknad(søknadsId = søknadsId, sendtNav = null, sendtArbeidsgiver = LocalDateTime.now()))
        leggOppgavePåAivenKafka(
            OppgaveDTO(
                dokumentType = DokumentTypeDTO.Søknad,
                oppdateringstype = OppdateringstypeDTO.Utsett,
                dokumentId = søknadsId,
                timeout = omFireTimer
            )
        )

        val oppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Utsett).isEqualTo(oppgave.status)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgave.timeout
        assertThat(oppgave.avstemt).isFalse

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(søknadsId.toString()))
        assertThat(OppgaveStatus.Utsett).isEqualTo(oppgaveFraAiven.status)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgaveFraAiven.timeout
        assertThat(oppgaveFraAiven.avstemt).isFalse
    }

    private fun leggSøknadPåKafka(søknad: SykepengesoknadDTO) =
        aivenSoknadSendtListener.listen(skapConsumerRecord("key", søknad.serialisertTilString()), acknowledgment)

    private fun leggOppgavePåAivenKafka(oppgave: OppgaveDTO) =
        aivenSpreOppgaverListener.listen(skapConsumerRecord("key", oppgave.serialisertTilString()), acknowledgment)

    private fun søknad(
        søknadsId: UUID = UUID.randomUUID(),
        sendtNav: LocalDateTime? = LocalDateTime.now(),
        sendtArbeidsgiver: LocalDateTime? = null
    ) = SykepengesoknadDTO(
        fnr = fnr,
        id = søknadsId.toString(),
        opprettet = LocalDateTime.now(),
        fom = LocalDate.of(2019, 5, 4),
        tom = LocalDate.of(2019, 5, 8),
        type = SoknadstypeDTO.ARBEIDSTAKERE,
        sporsmal = listOf(
            SporsmalDTO(
                id = UUID.randomUUID().toString(),
                tag = "TAGGEN",
                sporsmalstekst = "Har systemet gode integrasjonstester?",
                svartype = SvartypeDTO.JA_NEI,
                svar = listOf(SvarDTO(verdi = "JA"))

            )
        ),
        status = SoknadsstatusDTO.SENDT,
        sendtNav = sendtNav,
        sendtArbeidsgiver = sendtArbeidsgiver,
    )
}

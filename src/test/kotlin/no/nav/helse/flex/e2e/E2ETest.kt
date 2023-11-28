package no.nav.helse.flex.e2e

import com.nhaarman.mockitokotlin2.whenever
import no.nav.helse.flex.FellesTestoppsett
import no.nav.helse.flex.any
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.repository.InnsendingDbRecord
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.SaksbehandlingsService
import no.nav.helse.flex.`should be equal to ignoring nano and zone`
import no.nav.helse.flex.skapConsumerRecord
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SporsmalDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvarDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SvartypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.helse.flex.util.tilOsloZone
import org.amshove.kluent.`should be equal to`
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@DirtiesContext
class E2ETest : FellesTestoppsett() {

    companion object {
        const val aktorId = "aktørId"
        const val fnr = "fnr"
        val omFireTimer: LocalDateTime = LocalDateTime.now().plusHours(4)
    }

    @MockBean
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @BeforeEach
    fun setup() {
        whenever(saksbehandlingsService.finnEksisterendeInnsending(any())).thenAnswer {
            InnsendingDbRecord(
                id = "iid",
                sykepengesoknadId = it.arguments[0].toString(),
                journalpostId = "journalpost"
            )
        }
    }

    @Test
    fun `Bømlo sier Opprett før vi behandler søknad`() {
        val soknadId = UUID.randomUUID()
        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId, null))

        val oppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(OppgaveStatus.Opprett).isEqualTo(oppgave.status)
        assertThat(oppgave.timeout).isNull()
        assertThat(oppgave.avstemt).isFalse

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(OppgaveStatus.Opprett).isEqualTo(oppgaveFraAiven.status)
        assertThat(oppgaveFraAiven.timeout).isNull()
        assertThat(oppgaveFraAiven.avstemt).isFalse
    }

    @Test
    fun `Bømlo sier Utsett før vi behandler søknad`() {
        val soknadId = UUID.randomUUID()
        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, soknadId, omFireTimer))

        val oppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(OppgaveStatus.Utsett).isEqualTo(oppgave.status)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgave.timeout
        assertThat(oppgave.avstemt).isFalse

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(OppgaveStatus.Utsett).isEqualTo(oppgaveFraAiven.status)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgaveFraAiven.timeout
        assertThat(oppgaveFraAiven.avstemt).isFalse
    }

    @Test
    fun `Bømlo sier Ferdigbehandlet før vi behandler søknad`() {
        val soknadId = UUID.randomUUID()
        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Ferdigbehandlet, soknadId, null))

        val oppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(OppgaveStatus.IkkeOpprett).isEqualTo(oppgave.status)
        assertThat(oppgave.timeout).isNull()
        assertThat(oppgave.avstemt).isFalse

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(oppgaveFraAiven.status).isEqualTo(OppgaveStatus.IkkeOpprett)
        assertThat(oppgaveFraAiven.timeout).isNull()
        assertThat(oppgaveFraAiven.avstemt).isFalse
    }

    @Test
    fun `vi behandler søknad så kommer Utsett fra Bømlo`() {
        val soknadId = UUID.randomUUID()
        leggSoknadPaKafka(lagSoknad(soknadId))
        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, soknadId, omFireTimer))

        val oppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(oppgave.status).isEqualTo(OppgaveStatus.Utsett)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgave.timeout
        assertThat(oppgave.avstemt).isTrue

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(oppgaveFraAiven.status).isEqualTo(OppgaveStatus.Utsett)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgaveFraAiven.timeout
        assertThat(oppgaveFraAiven.avstemt).isTrue
    }

    @Test
    fun `vi behandler søknad så kommer Opprett fra Bømlo`() {
        val soknadId = UUID.randomUUID()
        leggSoknadPaKafka(lagSoknad(soknadId))
        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId))

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(oppgaveFraAiven.status).isEqualTo(OppgaveStatus.Opprett)
        assertThat(oppgaveFraAiven.timeout).isNull()
        assertThat(oppgaveFraAiven.avstemt).isTrue
    }

    @Test
    fun `vi behandler søknad så kommer Ferdigbehandlet fra Bømlo`() {
        val soknadId = UUID.randomUUID()
        leggSoknadPaKafka(lagSoknad(soknadId))
        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Ferdigbehandlet, soknadId))

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(oppgaveFraAiven.status).isEqualTo(OppgaveStatus.IkkeOpprett)
        assertThat(oppgaveFraAiven.timeout).isNull()
        assertThat(oppgaveFraAiven.avstemt).isTrue
    }

    @Test
    fun `Bømlo sier Utsett så behandler vi søknaden og utsetter oppgave`() {
        val soknadId = UUID.randomUUID()
        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, soknadId, omFireTimer))
        leggSoknadPaKafka(lagSoknad(soknadId))

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(oppgaveFraAiven.status).isEqualTo(OppgaveStatus.Utsett)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgaveFraAiven.timeout!!
        assertThat(oppgaveFraAiven.avstemt).isTrue
    }

    @Test
    fun `oppgaven timer ut og vi oppretter oppgave`() {
        val soknadId = UUID.randomUUID()
        val timeout = LocalDateTime.now().minusHours(1)

        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, soknadId, timeout))
        leggSoknadPaKafka(lagSoknad(soknadId))

        oppgaveOpprettelse.behandleOppgaver()

        val behandletOppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(behandletOppgave.status).isEqualTo(OppgaveStatus.OpprettetTimeout)
        assertThat(behandletOppgave.timeout).isNull()
    }

    @Test
    fun `Bømlo sier Opprett og oppgave får Opprettet status`() {
        val soknadId = UUID.randomUUID()

        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Opprett, soknadId, omFireTimer))
        leggSoknadPaKafka(lagSoknad(soknadId))

        oppgaveOpprettelse.behandleOppgaver()

        val behandletOppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(behandletOppgave.status).isEqualTo(OppgaveStatus.Opprettet)
        assertThat(behandletOppgave.timeout).isNull()
    }

    @Test
    fun `Bømlo sier OpprettSpeilRelatert og oppgave får OpprettetSpeilRelatert status`() {
        val soknadId = UUID.randomUUID()

        leggOppgavePaAivenKafka(
            OppgaveDTO(
                DokumentTypeDTO.Søknad,
                OppdateringstypeDTO.OpprettSpeilRelatert,
                soknadId,
                omFireTimer
            )
        )
        leggSoknadPaKafka(lagSoknad(soknadId))

        oppgaveOpprettelse.behandleOppgaver()

        val behandletOppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(behandletOppgave.status).isEqualTo(OppgaveStatus.OpprettetSpeilRelatert)
        assertThat(behandletOppgave.timeout).isNull()
    }

    @Test
    fun `Bømlo sier Utsett etter oppgaven er opprettet`() {
        val soknadId = UUID.randomUUID()
        val timeout = LocalDateTime.now().minusHours(1)
        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, soknadId, timeout))
        leggSoknadPaKafka(lagSoknad(soknadId))

        oppgaveOpprettelse.behandleOppgaver()

        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, soknadId, omFireTimer))

        val behandletOppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(behandletOppgave.status).isEqualTo(OppgaveStatus.OpprettetTimeout)
        assertThat(behandletOppgave.timeout).isNull()
    }

    @Test
    fun `Bømlo sier Utsett etter oppgaven er ferdigbehandlet`() {
        val soknadId = UUID.randomUUID()
        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Ferdigbehandlet, soknadId))
        leggSoknadPaKafka(lagSoknad(soknadId))
        leggOppgavePaAivenKafka(OppgaveDTO(DokumentTypeDTO.Søknad, OppdateringstypeDTO.Utsett, soknadId, omFireTimer))

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(oppgaveFraAiven.status).isEqualTo(OppgaveStatus.IkkeOpprett)
        assertThat(oppgaveFraAiven.timeout).isNull()
    }

    @Test
    fun `Bømlo sier Utsett så behandler vi søknaden og ikke oppretter oppgave`() {
        val soknadId = UUID.randomUUID()
        leggOppgavePaAivenKafka(
            OppgaveDTO(
                dokumentType = DokumentTypeDTO.Søknad,
                oppdateringstype = OppdateringstypeDTO.Utsett,
                dokumentId = soknadId,
                timeout = LocalDateTime.now().plusHours(1)
            )
        )

        val oppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(oppgave.status).isEqualTo(OppgaveStatus.Utsett)

        assertThat(oppgave.avstemt).isFalse
        leggSoknadPaKafka(lagSoknad(soknadId = soknadId, sendtNav = null, sendtArbeidsgiver = LocalDateTime.now()))

        oppgaveOpprettelse.behandleOppgaver()

        val behandletOppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(behandletOppgave.status).isEqualTo(OppgaveStatus.Utsett)
        assertThat(behandletOppgave.avstemt).isTrue
    }

    @Test
    fun `vi behandler søknaden uten oppgave så sender Bømlo Utsett`() {
        val soknadId = UUID.randomUUID()
        leggSoknadPaKafka(lagSoknad(soknadId = soknadId, sendtNav = null, sendtArbeidsgiver = LocalDateTime.now()))
        leggOppgavePaAivenKafka(
            OppgaveDTO(
                dokumentType = DokumentTypeDTO.Søknad,
                oppdateringstype = OppdateringstypeDTO.Utsett,
                dokumentId = soknadId,
                timeout = omFireTimer
            )
        )

        val behandletOppgave = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(behandletOppgave.status).isEqualTo(OppgaveStatus.Utsett)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` behandletOppgave.timeout
        assertThat(behandletOppgave.avstemt).isTrue

        val oppgaveFraAiven = requireNotNull(spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString()))
        assertThat(oppgaveFraAiven.status).isEqualTo(OppgaveStatus.Utsett)
        omFireTimer.tilOsloZone() `should be equal to ignoring nano and zone` oppgaveFraAiven.timeout
        assertThat(oppgaveFraAiven.avstemt).isTrue
    }

    @Test
    fun `tidspunkt for opprettet og modifisert er likt etter opprettelse`() {
        val soknadId = UUID.randomUUID()
        leggOppgavePaAivenKafka(
            OppgaveDTO(
                DokumentTypeDTO.Søknad,
                OppdateringstypeDTO.Opprett,
                soknadId,
                omFireTimer
            )
        )
        leggSoknadPaKafka(lagSoknad(soknadId))

        val opprettetOppgave = spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString())

        opprettetOppgave?.opprettet `should be equal to` opprettetOppgave?.modifisert
    }

    @Test
    fun `tidspunkt for modifisert blir oppdatert når det lages oppgave`() {
        val soknadId = UUID.randomUUID()
        leggOppgavePaAivenKafka(
            OppgaveDTO(
                DokumentTypeDTO.Søknad,
                OppdateringstypeDTO.Opprett,
                soknadId,
                omFireTimer
            )
        )
        leggSoknadPaKafka(lagSoknad(soknadId))

        val modifisertTidspunkt = Instant.now()
        oppgaveOpprettelse.behandleOppgaver(modifisertTidspunkt)

        val modifisertOppgave = spreOppgaveRepository.findBySykepengesoknadId(soknadId.toString())

        modifisertOppgave!!.modifisert `should be equal to ignoring nano and zone` modifisertTidspunkt
    }

    private fun leggSoknadPaKafka(soknad: SykepengesoknadDTO) =
        aivenSoknadSendtListener.listen(skapConsumerRecord("key", soknad.serialisertTilString()), acknowledgment)

    private fun leggOppgavePaAivenKafka(oppgave: OppgaveDTO) =
        aivenSpreOppgaverListener.listen(skapConsumerRecord("key", oppgave.serialisertTilString()), acknowledgment)

    private fun lagSoknad(
        soknadId: UUID = UUID.randomUUID(),
        sendtNav: LocalDateTime? = LocalDateTime.now(),
        sendtArbeidsgiver: LocalDateTime? = null
    ) = SykepengesoknadDTO(
        fnr = fnr,
        id = soknadId.toString(),
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
        sendtArbeidsgiver = sendtArbeidsgiver
    )
}

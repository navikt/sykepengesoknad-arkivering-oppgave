package no.nav.syfo.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.arkivering.Arkivaren
import no.nav.syfo.client.FlexBucketUploaderClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.domain.Innsending
import no.nav.syfo.domain.PdfKvittering
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.producer.RebehandleSykepengesoknadProducer
import no.nav.syfo.logger
import no.nav.syfo.repository.InnsendingDAO
import org.springframework.stereotype.Component
import java.util.*

@Component
class SaksbehandlingsService(
    private val oppgaveService: OppgaveService,
    private val arkivaren: Arkivaren,
    private val innsendingDAO: InnsendingDAO,
    private val registry: MeterRegistry,
    private val rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer,
    private val flexBucketUploaderClient: FlexBucketUploaderClient,
    private val identService: IdentService,
    private val pdlClient: PdlClient,
) {

    private val log = logger()

    fun behandleSoknad(sykepengesoknad: Sykepengesoknad): String {
        val eksisterendeInnsending = finnEksisterendeInnsending(sykepengesoknad.id)
        val innsendingId = eksisterendeInnsending?.id
            ?: innsendingDAO.opprettInnsending(
                sykepengesoknadId = sykepengesoknad.id,
                soknadFom = sykepengesoknad.fom,
                soknadTom = sykepengesoknad.tom
            )
        val fnr = identService.hentFnrForAktorId(sykepengesoknad.aktorId)

        val soknad = opprettSoknad(sykepengesoknad, fnr)
        if (eksisterendeInnsending?.journalpostId.isNullOrBlank()) {
            val jouralpostId = opprettJournalpost(innsendingId, soknad)
            log.info("Journalført søknad: ${sykepengesoknad.id} med journalpostId: $jouralpostId")
        }
        return innsendingId
    }

    fun opprettOppgave(sykepengesoknad: Sykepengesoknad, innsending: Innsending, speilRelatert: Boolean = false) {
        val fnr = identService.hentFnrForAktorId(sykepengesoknad.aktorId)

        val soknad = opprettSoknad(sykepengesoknad, fnr)

        val requestBody = OppgaveService.lagRequestBody(
            journalpostId = innsending.journalpostId!!,
            soknad = soknad,
            harRedusertVenteperiode = sykepengesoknad.harRedusertVenteperiode,
            speilRelatert = speilRelatert,
        )
        val oppgaveId = oppgaveService.opprettOppgave(requestBody).id.toString()

        innsendingDAO.oppdaterOppgaveId(uuid = innsending.id, oppgaveId = oppgaveId)

        tellInnsendingBehandlet(soknad.soknadstype)
        log.info("Oppretter oppgave ${innsending.id} for ${soknad.soknadstype.name.lowercase()} søknad: ${soknad.soknadsId}")
    }

    fun settFerdigbehandlet(innsendingsId: String) {
        innsendingDAO.settBehandlet(innsendingsId)
    }

    fun opprettJournalpost(innsendingId: String, soknad: Soknad): String {
        val journalpostId = arkivaren.opprettJournalpost(soknad = soknad)
        innsendingDAO.oppdaterJournalpostId(innsendingId, journalpostId)
        return journalpostId
    }

    fun finnEksisterendeInnsending(sykepengesoknadId: String) =
        innsendingDAO.finnInnsendingForSykepengesoknad(sykepengesoknadId)

    fun innsendingFeilet(sykepengesoknad: Sykepengesoknad, e: Exception) {
        val eksisterendeInnsending = finnEksisterendeInnsending(sykepengesoknad.id)
        tellInnsendingFeilet(sykepengesoknad.soknadstype)
        log.error(
            "Kunne ikke fullføre innsending av søknad med innsending id: {} og sykepengesøknad id: {}, legger på intern rebehandling-topic",
            eksisterendeInnsending?.id,
            sykepengesoknad.id,
            e
        )
        rebehandleSykepengesoknadProducer.send(sykepengesoknad)
    }

    fun opprettSoknad(sykepengesoknad: Sykepengesoknad, fnr: String): Soknad {
        val navn = pdlClient.hentFormattertNavn(fnr)

        val soknad = Soknad.lagSoknad(sykepengesoknad, fnr, navn)

        return soknad.copy(kvitteringer = soknad.kvitteringer?.map { it.hentOgSettKvittering() })
    }

    private fun PdfKvittering.hentOgSettKvittering(): PdfKvittering {

        return this.copy(
            b64data = Base64.getEncoder().encodeToString(flexBucketUploaderClient.hentVedlegg(this.blobId))
        )
    }

    private fun tellInnsendingBehandlet(soknadstype: Soknadstype?) {
        registry.counter(
            "syfogsak.innsending.behandlet",
            Tags.of(
                "type", "info",
                "soknadstype", soknadstype?.name ?: "UKJENT",
                "help", "Antall ferdigbehandlede innsendinger."
            )
        ).increment()
    }

    private fun tellInnsendingFeilet(soknadstype: Soknadstype?) {
        registry.counter(
            "syfogsak.innsending.feilet",
            Tags.of(
                "type", "info",
                "soknadstype", soknadstype?.name ?: "UKJENT",
                "help", "Antall innsendinger hvor feil mot baksystemer gjorde at behandling ikke kunne fullføres."
            )
        ).increment()
    }
}

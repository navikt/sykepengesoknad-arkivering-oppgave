package no.nav.helse.flex.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.helse.flex.client.FlexBucketUploaderClient
import no.nav.helse.flex.client.pdl.PdlClient
import no.nav.helse.flex.domain.PdfKvittering
import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.kafka.producer.RebehandleSykepengesoknadProducer
import no.nav.helse.flex.logger
import no.nav.helse.flex.repository.InnsendingDbRecord
import no.nav.helse.flex.repository.InnsendingRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class SaksbehandlingsService(
    private val oppgaveService: OppgaveService,
    private val arkivaren: no.nav.helse.flex.arkivering.Arkivaren,
    private val innsendingRepository: InnsendingRepository,
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
            ?: innsendingRepository.save(
                InnsendingDbRecord(
                    sykepengesoknadId = sykepengesoknad.id
                )
            ).id
        val fnr = identService.hentFnrForAktorId(sykepengesoknad.aktorId)

        val soknad = opprettSoknad(sykepengesoknad, fnr)
        if (eksisterendeInnsending?.journalpostId.isNullOrBlank()) {
            val jouralpostId = opprettJournalpost(
                innsendingId!!,
                soknad
            )
            log.info("Journalført søknad: ${sykepengesoknad.id} med journalpostId: $jouralpostId")
        }
        return innsendingId!!
    }

    fun opprettOppgave(sykepengesoknad: Sykepengesoknad, innsending: InnsendingDbRecord, speilRelatert: Boolean = false) {
        val fnr = identService.hentFnrForAktorId(sykepengesoknad.aktorId)

        val soknad = opprettSoknad(sykepengesoknad, fnr)

        val requestBody = OppgaveService.lagRequestBody(
            aktorId = sykepengesoknad.aktorId,
            journalpostId = innsending.journalpostId!!,
            soknad = soknad,
            harRedusertVenteperiode = sykepengesoknad.harRedusertVenteperiode,
            speilRelatert = speilRelatert,
        )
        val oppgaveId = oppgaveService.opprettOppgave(requestBody).id.toString()

        innsendingRepository.updateOppgaveId(id = innsending.id!!, oppgaveId = oppgaveId)

        tellInnsendingBehandlet(soknad.soknadstype)
        log.info("Oppretter oppgave ${innsending.id} for ${soknad.soknadstype.name.lowercase()} søknad: ${soknad.soknadsId}")
    }

    fun settFerdigbehandlet(innsendingsId: String) {
        innsendingRepository.updateBehandlet(innsendingsId)
    }

    fun opprettJournalpost(innsendingId: String, soknad: Soknad): String {
        val journalpostId = arkivaren.opprettJournalpost(soknad = soknad)
        innsendingRepository.updateJournalpostId(id = innsendingId, journalpostId = journalpostId)
        return journalpostId
    }

    fun finnEksisterendeInnsending(sykepengesoknadId: String) =
        innsendingRepository.findBySykepengesoknadId(sykepengesoknadId)

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

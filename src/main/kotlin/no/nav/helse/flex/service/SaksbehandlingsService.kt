package no.nav.helse.flex.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.helse.flex.arkivering.Arkivaren
import no.nav.helse.flex.client.SykepengesoknadKvitteringerClient
import no.nav.helse.flex.client.pdl.PdlClient
import no.nav.helse.flex.domain.PdfKvittering
import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.kafka.producer.RebehandleSykepengesoknadProducer
import no.nav.helse.flex.logger
import no.nav.helse.flex.medlemskap.MedlemskapVurdering
import no.nav.helse.flex.medlemskap.MedlemskapVurderingRepository
import no.nav.helse.flex.repository.InnsendingDbRecord
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.tilbakedaterte.OppgaverForTilbakedaterteDbRecord
import no.nav.helse.flex.tilbakedaterte.OppgaverForTilbakedaterteRepository
import no.nav.helse.flex.tilbakedaterte.OppgaverForTilbakedaterteStatus
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class SaksbehandlingsService(
    private val oppgaveClient: OppgaveClient,
    private val arkivaren: Arkivaren,
    private val innsendingRepository: InnsendingRepository,
    private val registry: MeterRegistry,
    private val rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer,
    private val oppgaverForTilbakedaterteRepository: OppgaverForTilbakedaterteRepository,
    private val sykepengesoknadKvitteringerClient: SykepengesoknadKvitteringerClient,
    private val identService: IdentService,
    private val pdlClient: PdlClient,
    private val medlemskapVurdering: MedlemskapVurdering,
    private val medlemskapVurderingRepository: MedlemskapVurderingRepository,
) {
    private val log = logger()

    fun behandleSoknad(sykepengesoknad: Sykepengesoknad): String {
        val eksisterendeInnsending = finnEksisterendeInnsending(sykepengesoknad.id)
        val innsendingId =
            eksisterendeInnsending?.id
                ?: innsendingRepository
                    .save(
                        InnsendingDbRecord(
                            sykepengesoknadId = sykepengesoknad.id,
                        ),
                    ).id
        val fnr = identService.hentFnrForAktorId(sykepengesoknad.aktorId)

        val soknad = opprettSoknad(sykepengesoknad, fnr)
        if (eksisterendeInnsending?.journalpostId.isNullOrBlank()) {
            val jouralpostId =
                opprettJournalpost(
                    innsendingId!!,
                    soknad,
                )
            log.info("Journalført søknad: ${sykepengesoknad.id} med journalpostId: $jouralpostId.")
        }
        medlemskapVurdering.oppdaterInngåendeMedlemskapVurdering(sykepengesoknad)

        return innsendingId!!
    }

    fun opprettOppgave(
        sykepengesoknad: Sykepengesoknad,
        innsending: InnsendingDbRecord,
        speilRelatert: Boolean = false,
    ) {
        val fnr = identService.hentFnrForAktorId(sykepengesoknad.aktorId)
        val endeligVurdering =
            hentEndeligMedlemskapVurdering(
                sykepengesoknad,
                medlemskapVurderingRepository.findBySykepengesoknadId(sykepengesoknad.id)?.inngaendeVurdering,
            )
        val soknad = opprettSoknad(sykepengesoknad, fnr, endeligVurdering)

        val behandlingstemaOgType =
            finnBehandlingstemaOgType(
                soknad = sykepengesoknad,
                harRedusertVenteperiode = sykepengesoknad.harRedusertVenteperiode,
                speilRelatert = speilRelatert,
                medlemskapVurdering = endeligVurdering,
            )

        val requestBody =
            lagOppgaveRequest(
                aktorId = sykepengesoknad.aktorId,
                journalpostId = innsending.journalpostId!!,
                soknad = soknad,
                behandlingstemaOgType = behandlingstemaOgType,
            )
        val oppgaveResponse = oppgaveClient.opprettOppgave(requestBody)

        innsendingRepository.updateOppgaveId(id = innsending.id!!, oppgaveId = oppgaveResponse.id.toString())
        if (requestBody.behandlingstype == BEHANDLINGSTEMA_TILBAKEDATERING) {
            oppgaverForTilbakedaterteRepository.save(
                OppgaverForTilbakedaterteDbRecord(
                    sykepengesoknadUuid = sykepengesoknad.id,
                    sykmeldingUuid = sykepengesoknad.sykmeldingId ?: throw RuntimeException("SykmeldingId mangler"),
                    oppgaveId = oppgaveResponse.id.toString(),
                    opprettet = Instant.now(),
                    oppdatert = null,
                    status = OppgaverForTilbakedaterteStatus.OPPRETTET,
                ),
            )
        }
        tellInnsendingBehandlet(soknad.soknadstype)
        log.info("Oppretter oppgave ${innsending.id} for ${soknad.soknadstype.name.lowercase()} søknad: ${soknad.soknadsId}")
    }

    fun settFerdigbehandlet(innsendingsId: String) {
        innsendingRepository.updateBehandlet(innsendingsId)
    }

    fun opprettJournalpost(
        innsendingId: String,
        soknad: Soknad,
    ): String {
        val journalpostId = arkivaren.opprettJournalpost(soknad = soknad)
        innsendingRepository.updateJournalpostId(id = innsendingId, journalpostId = journalpostId)
        return journalpostId
    }

    fun finnEksisterendeInnsending(sykepengesoknadId: String) = innsendingRepository.findBySykepengesoknadId(sykepengesoknadId)

    fun innsendingFeilet(
        sykepengesoknad: Sykepengesoknad,
        e: Exception,
    ) {
        val eksisterendeInnsending = finnEksisterendeInnsending(sykepengesoknad.id)
        tellInnsendingFeilet(sykepengesoknad.soknadstype)
        log.error(
            "Kunne ikke fullføre innsending av søknad med innsending id: {} og sykepengesøknad id: {}, legger på intern rebehandling-topic",
            eksisterendeInnsending?.id,
            sykepengesoknad.id,
            e,
        )
        rebehandleSykepengesoknadProducer.send(sykepengesoknad)
    }

    fun opprettSoknad(
        sykepengesoknad: Sykepengesoknad,
        fnr: String,
        endeligMedlemskapVurdering: String? = null,
    ): Soknad {
        val navn = pdlClient.hentFormattertNavn(fnr)

        val soknad = Soknad.lagSoknad(sykepengesoknad, fnr, navn, endeligMedlemskapVurdering)

        return soknad.copy(kvitteringer = soknad.kvitteringer?.map { it.hentOgSettKvittering() })
    }

    private fun PdfKvittering.hentOgSettKvittering(): PdfKvittering =
        this.copy(
            b64data = Base64.getEncoder().encodeToString(sykepengesoknadKvitteringerClient.hentVedlegg(this.blobId)),
        )

    private fun tellInnsendingBehandlet(soknadstype: Soknadstype?) {
        registry
            .counter(
                "innsending.behandlet",
                Tags.of(
                    "type",
                    "info",
                    "soknadstype",
                    soknadstype?.name ?: "UKJENT",
                    "help",
                    "Antall ferdigbehandlede innsendinger.",
                ),
            ).increment()
    }

    private fun tellInnsendingFeilet(soknadstype: Soknadstype?) {
        registry
            .counter(
                "innsending.feilet",
                Tags.of(
                    "type",
                    "info",
                    "soknadstype",
                    soknadstype?.name ?: "UKJENT",
                    "help",
                    "Antall innsendinger hvor feil mot baksystemer gjorde at behandling ikke kunne fullføres.",
                ),
            ).increment()
    }

    private fun Sykepengesoknad.harMedlemskapSporsmal(): Boolean = this.sporsmal.any { it.tag.startsWith("MEDLEMSKAP_") }

    private fun hentEndeligMedlemskapVurdering(
        sykepengesoknad: Sykepengesoknad,
        inngaendeVurdering: String?,
    ): String? =
        when (inngaendeVurdering) {
            "UAVKLART" -> {
                if (sykepengesoknad.harMedlemskapSporsmal()) {
                    // Returnerer UAVKLART hvis endeligVurdering er null er siden det kan skyldes at LovMe ikke har
                    // svart selv om vi vet at bruker har svart på medlemskapsspørsmål.
                    medlemskapVurdering.hentEndeligMedlemskapVurdering(sykepengesoknad) ?: "UAVKLART"
                } else {
                    null
                }
            }
            // Returnerer NEI so, endelig vurdring hvis inngående vurdering er NEI, siden vi da ikke spør om
            // endelig vurdering, men NEI skal behandles som en medlemskapsoppgave.
            "NEI" -> "NEI"
            else -> null
        }
}

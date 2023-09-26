package no.nav.helse.flex.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.helse.flex.client.SykepengesoknadKvitteringerClient
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
import org.springframework.web.client.HttpClientErrorException
import java.util.*

@Component
class SaksbehandlingsService(
    private val oppgaveService: OppgaveService,
    private val arkivaren: no.nav.helse.flex.arkivering.Arkivaren,
    private val innsendingRepository: InnsendingRepository,
    private val registry: MeterRegistry,
    private val rebehandleSykepengesoknadProducer: RebehandleSykepengesoknadProducer,
    private val sykepengesoknadKvitteringerClient: SykepengesoknadKvitteringerClient,
    private val identService: IdentService,
    private val pdlClient: PdlClient
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

    fun opprettOppgave(
        sykepengesoknad: Sykepengesoknad,
        innsending: InnsendingDbRecord,
        speilRelatert: Boolean = false
    ) {
        val fnr = identService.hentFnrForAktorId(sykepengesoknad.aktorId)

        val soknad = opprettSoknad(sykepengesoknad, fnr)

        val requestBody = OppgaveService.lagRequestBody(
            aktorId = sykepengesoknad.aktorId,
            journalpostId = innsending.journalpostId!!,
            soknad = soknad,
            harRedusertVenteperiode = sykepengesoknad.harRedusertVenteperiode,
            speilRelatert = speilRelatert
        )
        val oppgaveResponse = oppgaveService.opprettOppgave(requestBody)

        innsendingRepository.updateOppgaveId(id = innsending.id!!, oppgaveId = oppgaveResponse.id.toString())

        tellInnsendingBehandlet(soknad.soknadstype)
        log.info("Oppretter oppgave ${innsending.id} for ${soknad.soknadstype.name.lowercase()} søknad: ${soknad.soknadsId}")

        if (soknad.soknadstype == Soknadstype.OPPHOLD_UTLAND) {
            sjekkOmOppholdUtlandSendesTilEnhet4488(soknad, requestBody, oppgaveResponse)
        }
    }

    private fun sjekkOmOppholdUtlandSendesTilEnhet4488(
        soknad: Soknad,
        req: OppgaveRequest,
        res: OppgaveResponse
    ) {
        if (res.tildeltEnhetsnr == "4488") {
            log.warn(
                "Søknad om opphold utland ${soknad.soknadsId} ble tildelt enhet ${res.tildeltEnhetsnr}. Request = ${
                req.copy(
                    aktoerId = "***",
                    beskrivelse = "***"
                )
                }. Response $res"
            )
        }
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
        fun PdfKvittering.hentOgSettKvittering(): PdfKvittering {
            return try {
                this.copy(
                    b64data = Base64.getEncoder()
                        .encodeToString(sykepengesoknadKvitteringerClient.hentVedlegg(this.blobId))
                )
            } catch (e: HttpClientErrorException.NotFound) {
                if (sykepengesoknad.id == "3e7c6d64-8e24-3f0b-8e6e-b3211315789b") {
                    log.info("Bruker blankt bilde for korrigert kvittering i soknad ${sykepengesoknad.id}")
                    this.copy(
                        b64data = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD3+iiigD//2Q=="
                    )
                } else {
                    throw e
                }
            }
        }
        return soknad.copy(kvitteringer = soknad.kvitteringer?.map { it.hentOgSettKvittering() })
    }

    private fun tellInnsendingBehandlet(soknadstype: Soknadstype?) {
        registry.counter(
            "innsending.behandlet",
            Tags.of(
                "type",
                "info",
                "soknadstype",
                soknadstype?.name ?: "UKJENT",
                "help",
                "Antall ferdigbehandlede innsendinger."
            )
        ).increment()
    }

    private fun tellInnsendingFeilet(soknadstype: Soknadstype?) {
        registry.counter(
            "innsending.feilet",
            Tags.of(
                "type",
                "info",
                "soknadstype",
                soknadstype?.name ?: "UKJENT",
                "help",
                "Antall innsendinger hvor feil mot baksystemer gjorde at behandling ikke kunne fullføres."
            )
        ).increment()
    }
}

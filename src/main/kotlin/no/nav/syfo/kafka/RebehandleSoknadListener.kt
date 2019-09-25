package no.nav.syfo.kafka

import no.nav.syfo.config.CALL_ID
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.log
import no.nav.syfo.service.BehandleFeiledeSoknaderService
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.errors.WakeupException
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID.randomUUID

@Component
class RebehandleSoknadListener(
        private val behandleFeiledeSoknaderService: BehandleFeiledeSoknaderService,
        private val innsendingDAO: InnsendingDAO,
        @Value("\${fasit.environment.name}") miljonavn: String,
        consumerFactory: ConsumerFactory<String, Soknad>) {

    private val consumer: Consumer<String, Soknad>

    init {
        val groupId = "syfogsak-$miljonavn-rebehandleSoknad"
        consumer = consumerFactory.createConsumer(groupId, "rebehandleSoknad")
        consumer.subscribe(listOf("syfo-soknad-v2"))
    }

    private fun Sykepengesoknad.sendtTilNav(): Boolean =
            status == "SENDT" && sendtNav != null

    private fun Sykepengesoknad.ettersendtTilArbeidsgiver(): Boolean =
            sendtArbeidsgiver != null && sendtNav?.isBefore(sendtArbeidsgiver) ?: false

    private val log = log()

    @Scheduled(cron = "0 0 * * * *")
    fun listen() {
        val feilendeInnsendinger = innsendingDAO.hentFeilendeInnsendinger()
        var antallFeilende = 0

        consumer.poll(100L)
        consumer.seekToBeginning(consumer.assignment())
        try {
            while (true) {
                val records = consumer.poll(1000L)
                        .takeUnless { it.isEmpty }
                        ?: break
                records.forEach { record ->
                    log.debug("Melding mottatt på topic: {}, partisjon: {} med offset: {}",
                            record.topic(), record.partition(), record.offset())
                    try {
                        MDC.put(CALL_ID, getLastHeaderByKeyAsString(record.headers(), CALL_ID).orElseGet { randomUUID().toString() })

                        val sykepengesoknad =
                                when (val value = record.value()) {
                                    is SykepengesoknadDTO -> value.toSykepengesoknad()
                                    is SoknadDTO -> value.toSykepengesoknad()
                                    else -> null
                                }

                        sykepengesoknad
                                ?.takeIf { it.sendtTilNav() }
                                ?.takeUnless { it.ettersendtTilArbeidsgiver() }
                                ?.also { soknad ->
                                    feilendeInnsendinger
                                            .firstOrNull { innsending -> innsending.ressursId == soknad.id }
                                            ?.also { innsending ->
                                                log.info("Rebehandler søknad med id ${innsending.ressursId}")
                                                behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, soknad)
                                            }
                                }
                    } catch (e: Exception) {
                        log.warn("Uventet feil ved behandling av søknad", e)
                        antallFeilende++
                    } finally {
                        MDC.remove(CALL_ID)
                    }
                }
            }
        } catch (e: WakeupException) {
            // ignore for shutdown
        }

        if(antallFeilende > 0)
            log.info("Rebehandling av søknad feiler {} ganger.", antallFeilende)
    }
}

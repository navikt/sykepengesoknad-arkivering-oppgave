package no.nav.syfo.kafka

import no.nav.syfo.config.CALL_ID
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SykepengesoknadArbeidsledigDTO
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
class RebehandleSoknadarbeidsledigListener(
    private val behandleFeiledeSoknaderService: BehandleFeiledeSoknaderService,
    private val innsendingDAO: InnsendingDAO,
    @Value("\${fasit.environment.name}") miljonavn: String,
    consumerFactory: ConsumerFactory<String, SykepengesoknadArbeidsledigDTO>) {

    private val consumer: Consumer<String, SykepengesoknadArbeidsledigDTO>

    init {
        val groupId = "syfogsak-$miljonavn-rebehandleSoknadatbeidsledig"
        consumer = consumerFactory.createConsumer(groupId, "rebehandleSoknadarbeidsledig")
        consumer.subscribe(listOf("syfo-soknad-arbeidsledig-v1"))
    }

    private fun Sykepengesoknad.sendtTilNav(): Boolean =
        status == "SENDT" && sendtNav != null

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

                        val sykepengesoknad = record.value().toSykepengesoknad()

                        sykepengesoknad
                            .takeIf { it.sendtTilNav() }
                            ?.also { soknad ->
                                feilendeInnsendinger
                                    .firstOrNull { innsending -> innsending.ressursId == soknad.id }
                                    ?.also { innsending ->
                                        log.info("Rebehandler søknad arbeidsledig med id ${innsending.ressursId}")
                                        behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, soknad)
                                    }
                            }
                    } catch (e: Exception) {
                        log.warn("Uventet feil ved rebehandling av arbeidsledigsøknad", e)
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
            log.info("Rebehandling av søknad arbeidsledig feiler {} ganger.", antallFeilende)
    }
}

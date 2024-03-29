package no.nav.helse.flex.tilbakedaterte

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.sykmelding.kafka.model.SykmeldingKafkaMessage
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

const val SYKMELDINGSENDT_TOPIC = "teamsykmelding." + "syfo-sendt-sykmelding"
const val SYKMELDINGBEKREFTET_TOPIC = "teamsykmelding." + "syfo-bekreftet-sykmelding"

@Component
class SykmeldingSendtBekreftetConsumer(
    private val behandleSykmelding: BehandleSykmelding,
) {
    val log = logger()

    @KafkaListener(
        topics = [SYKMELDINGSENDT_TOPIC, SYKMELDINGBEKREFTET_TOPIC],
        id = "sykmelding-sendt-bekreftet",
        idIsGroup = false,
        containerFactory = "aivenKafkaListenerContainerFactory",
    )
    fun listen(
        cr: ConsumerRecord<String, String?>,
        acknowledgment: Acknowledgment,
    ) {
        MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))
        val melding = cr.value()?.tilSykmeldingKafkaMessage()

        try {
            behandleSykmelding.prosesserSykmelding(cr.key(), melding)
            acknowledgment.acknowledge()
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }
}

fun String.tilSykmeldingKafkaMessage(): SykmeldingKafkaMessage = objectMapper.readValue(this)

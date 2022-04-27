package no.nav.helse.flex.oppgavefordeling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.util.tilOsloZone
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SendtarbeidsgiverSoknader(
    val oppgavefordelingRepository: OppgavefordelingBatchRepository,
) {
    @KafkaListener(
        topics = [SENDT_SYKEPENGESOKNAD_TOPIC],
        id = "sendtarbeidsgiverSoknader",
        idIsGroup = true,
        concurrency = "12",
        containerFactory = "importKafkaListenerContainerFactory",
        properties = [
            "auto.offset.reset=earliest"
        ],
    )
    fun listenBatch(cr: List<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment) {

        val arbeidsgiverDatoer: ArrayList<ArbeidsgiverDato> = arrayListOf()

        cr.forEach {
            val soknad = it.value().tilEnkelSoknad()

            if (soknad.status == Soknadstatus.SENDT &&
                soknad.soknadstype == Soknadstype.ARBEIDSTAKERE &&
                soknad.sendtArbeidsgiver != null
            ) {
                arbeidsgiverDatoer.add(ArbeidsgiverDato(soknad.id, soknad.sendtArbeidsgiver.tilOsloZone().toInstant()))
            }
        }
        oppgavefordelingRepository.settSendtTilArbeidsGiver(arbeidsgiverDatoer)
        acknowledgment.acknowledge()
    }

    private data class EnkelSoknad(
        val id: String,
        val status: Soknadstatus,
        val sendtArbeidsgiver: LocalDateTime? = null,
        val soknadstype: Soknadstype,
    )

    private enum class Soknadstype {
        SELVSTENDIGE_OG_FRILANSERE,
        OPPHOLD_UTLAND,
        ARBEIDSTAKERE,
        ARBEIDSLEDIG,
        BEHANDLINGSDAGER,
        ANNET_ARBEIDSFORHOLD,
        REISETILSKUDD,
        GRADERT_REISETILSKUDD,
    }

    private enum class Soknadstatus {
        NY,
        SENDT,
        FREMTIDIG,
        UTKAST_TIL_KORRIGERING,
        KORRIGERT,
        AVBRUTT,
        UTGATT,
        SLETTET
    }

    private fun String.tilEnkelSoknad(): EnkelSoknad = objectMapper.readValue(this)
}

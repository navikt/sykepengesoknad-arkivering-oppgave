package no.nav.helse.flex.oppgavefordeling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class KorrigerteSoknader(
    val oppgavefordelingRepository: OppgavefordelingRepository,
) {

    private val log = logger()

    @KafkaListener(
        topics = [SENDT_SYKEPENGESOKNAD_TOPIC],
        id = "korrigerteSoknader",
        idIsGroup = true,
        concurrency = "6",
        containerFactory = "importKafkaListenerContainerFactory",
        properties = [
            "auto.offset.reset=earliest"
        ],
    )
    fun listenBatch(cr: List<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment) {
        cr.forEach {
            val soknad = it.value().tilEnkelSoknad()
            if (soknad.status == Soknadstatus.KORRIGERT && soknad.soknadstype == Soknadstype.ARBEIDSTAKERE) {
                oppgavefordelingRepository.settkorrigertAv(soknad.korrigertAv!!, soknad.id)
            }
        }
        acknowledgment.acknowledge()
    }

    data class EnkelSoknad(
        val id: String,
        val status: Soknadstatus,
        val soknadstype: Soknadstype,
        val korrigertAv: String? = null,
    )

    enum class Soknadstype {
        SELVSTENDIGE_OG_FRILANSERE,
        OPPHOLD_UTLAND,
        ARBEIDSTAKERE,
        ARBEIDSLEDIG,
        BEHANDLINGSDAGER,
        ANNET_ARBEIDSFORHOLD,
        REISETILSKUDD,
        GRADERT_REISETILSKUDD,
    }

    enum class Soknadstatus {
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

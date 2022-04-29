package no.nav.helse.flex.oppgavefordeling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class FodselsnummerSoknader(
    val oppgavefordelingRepository: OppgavefordelingBatchRepository,
) {
    @KafkaListener(
        topics = [SENDT_SYKEPENGESOKNAD_TOPIC],
        id = "fodselsnummerSoknader",
        idIsGroup = true,
        concurrency = "6",
        containerFactory = "importKafkaListenerContainerFactory",
        properties = [
            "auto.offset.reset=earliest"
        ],
    )
    fun listenBatch(cr: List<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment) {

        val fodselsnummere: ArrayList<Fodselsnummer> = arrayListOf()

        cr.forEach {
            val soknad = it.value().tilEnkelSoknad()

            if (soknad.status == Soknadstatus.SENDT &&
                !soknad.ettersendtTilArbeidsgiver() &&
                soknad.skalSynkeOppgaveOpprettelseMedBomlo() &&
                soknad.fnr != null
            ) {
                fodselsnummere.add(Fodselsnummer(soknad.id, soknad.fnr))
            }
        }
        oppgavefordelingRepository.settFodselsnummer(fodselsnummere)
        acknowledgment.acknowledge()
    }

    data class EnkelSoknad(
        val id: String,
        val status: Soknadstatus,
        val sendtNav: LocalDateTime? = null,
        val sendtArbeidsgiver: LocalDateTime? = null,
        val soknadstype: Soknadstype,
        val fnr: String?,
        val avbruttFeilinfo: Boolean? = null,
    ) {
        val sendTilGosys: Boolean?
            get() = avbruttFeilinfo
    }

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

    private fun EnkelSoknad.ettersendtTilArbeidsgiver() =
        sendtArbeidsgiver != null && sendtNav?.isBefore(sendtArbeidsgiver) ?: false

    private fun EnkelSoknad.skalSynkeOppgaveOpprettelseMedBomlo(): Boolean {
        return soknadstype == Soknadstype.ARBEIDSTAKERE && skalBehandlesAvNav() && this.sendTilGosys != true
    }

    private fun EnkelSoknad.skalBehandlesAvNav() =
        this.sendtNav != null

    private fun String.tilEnkelSoknad(): EnkelSoknad = objectMapper.readValue(this)
}

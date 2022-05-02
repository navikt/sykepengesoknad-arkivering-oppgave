package no.nav.helse.flex.oppgavefordeling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

const val SENDT_SYKEPENGESOKNAD_TOPIC = "flex." + "syfosoknad-sykepengesoknad-migrering"

@Component
class FomTomSoknader(
    val oppgavefordelingRepository: OppgavefordelingBatchRepository,
) {
    @KafkaListener(
        topics = [SENDT_SYKEPENGESOKNAD_TOPIC],
        id = "fomTomSoknader",
        idIsGroup = true,
        containerFactory = "importKafkaListenerContainerFactory",
        properties = [
            "auto.offset.reset=earliest"
        ],
    )
    fun listenBatch(cr: List<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment) {

        val liste: ArrayList<FomTom> = arrayListOf()

        cr.forEach {
            val soknad = it.value().tilEnkelSoknad()

            if (soknad.status == Soknadstatus.SENDT &&
                !soknad.ettersendtTilArbeidsgiver() &&
                soknad.skalSynkeOppgaveOpprettelseMedBomlo() &&
                soknad.fnr != null
            ) {
                liste.add(FomTom(soknad.id, soknad.fom, soknad.tom))
            }
        }
        oppgavefordelingRepository.settFomTom(liste)
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
        val fom: LocalDate?,
        val tom: LocalDate?,
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

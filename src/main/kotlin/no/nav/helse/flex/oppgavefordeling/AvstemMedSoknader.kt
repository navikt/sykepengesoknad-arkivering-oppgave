package no.nav.helse.flex.oppgavefordeling

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.util.tilOsloZone
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.LocalDateTime

const val SENDT_SYKEPENGESOKNAD_TOPIC = "flex." + "syfosoknad-sykepengesoknad-migrering"

@Component
class AvstemMedSoknader(
    val oppgavefordelingRepository: OppgavefordelingRepository,
) {

    @KafkaListener(
        topics = [SENDT_SYKEPENGESOKNAD_TOPIC],
        id = "avstemMedSoknader",
        idIsGroup = true,
        concurrency = "6",
        containerFactory = "aivenKafkaListenerContainerFactory",
        properties = [
            "auto.offset.reset=earliest"
        ],
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val soknad = cr.value().tilEnkelSoknad()

        if (soknad.status == "SENDT" && !soknad.ettersendtTilArbeidsgiver() && soknad.skalSynkeOppgaveOpprettelseMedBomlo()) {
            oppgavefordelingRepository.settTilAvstemt(
                soknad.id,
                soknad.sendtNav!!.tilOsloZone().toInstant()
            )
            acknowledgment.acknowledge()
        }
    }

    data class EnkelSoknad(
        val id: String,
        val status: String,
        val sendtNav: LocalDateTime? = null,
        val sendtArbeidsgiver: LocalDateTime? = null,
        val soknadstype: Soknadstype,
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

    private fun String.tilEnkelSoknad(): EnkelSoknad = objectMapper.readValue(this)

    private fun EnkelSoknad.ettersendtTilArbeidsgiver() =
        sendtArbeidsgiver != null && sendtNav?.isBefore(sendtArbeidsgiver) ?: false

    private fun EnkelSoknad.skalSynkeOppgaveOpprettelseMedBomlo(): Boolean {
        return soknadstype == Soknadstype.ARBEIDSTAKERE && skalBehandlesAvNav() && this.sendTilGosys != true
    }

    private fun EnkelSoknad.skalBehandlesAvNav() =
        this.sendtNav != null
}

package no.nav.helse.flex.kafka.consumer

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.kafka.mapper.toSykepengesoknad
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import no.nav.helse.flex.serialisertTilString
import no.nav.helse.flex.service.IdentService
import no.nav.helse.flex.spreoppgave.SpreOppgaverService
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MDC
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Duration

const val SYKEPENGESOKNAD_TOPIC = "flex." + "sykepengesoknad"

@Component
class AivenSoknadSendtListener(
    private val spreOppgaverService: SpreOppgaverService,
    private val identService: IdentService
) {

    private val log = logger()

    @KafkaListener(
        topics = [SYKEPENGESOKNAD_TOPIC],
        id = "soknadSendt",
        idIsGroup = false,
        concurrency = "3",
        containerFactory = "aivenKafkaListenerContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))

            val dto = cr.value().tilSykepengesoknadDTO()
            log.warn(dto.serialisertTilString())
            val aktorId = identService.hentAktorIdForFnr(dto.fnr)

            val sykepengesoknad = dto.toSykepengesoknad(aktorId)
            spreOppgaverService.soknadSendt(sykepengesoknad)

            acknowledgment.acknowledge()
        } catch (e: DbActionExecutionException) {
            if (e.cause is DuplicateKeyException) {
                log.info("Søknaden ${cr.key()} sin spre oppgave kan ikke legges inn i databasen nå, prøver igjen senere")
                acknowledgment.nack(Duration.ofMillis(100))
                return
            }
        } catch (e: Exception) {
            log.error("Uventet feil ved behandling av søknad ${cr.key()}", e)
            throw RuntimeException("Uventet feil ved behandling av søknad")
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }

    fun String.tilSykepengesoknadDTO(): SykepengesoknadDTO = objectMapper.readValue(this)
}

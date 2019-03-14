package no.nav.syfo.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.syfo.kafka.interfaces.Soknad;
import no.nav.syfo.kafka.soknad.dto.SoknadDTO;
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO;
import no.nav.syfo.service.SaksbehandlingsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static java.util.UUID.randomUUID;
import static no.nav.syfo.config.ApplicationConfig.CALL_ID;
import static no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString;
import static no.nav.syfo.kafka.mapper.SoknadDtoToSykepengesoknadMapperKt.toSykepengesoknad;
import static no.nav.syfo.kafka.mapper.SykepengesoknadDtoToSykepengesoknadMapperKt.toSykepengesoknad;

@Component
@Slf4j
public class SoknadSendtListener {

    private final SaksbehandlingsService saksbehandlingsService;

    @Inject
    public SoknadSendtListener(SaksbehandlingsService saksbehandlingsService) {
        this.saksbehandlingsService = saksbehandlingsService;
    }

    @KafkaListener(topics = "syfo-soknad-v2", id = "soknadSendt", idIsGroup = false)
    public void listen(ConsumerRecord<String, Soknad> cr, Acknowledgment acknowledgment) {
        log.debug("Melding mottatt på topic: {}, partisjon: {} med offset: {}", cr.topic(), cr.partition(), cr.offset());

        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID).orElseGet(randomUUID()::toString));

            Soknad soknad = cr.value();

            Sykepengesoknad sykepengesoknad = null;
            if (soknad instanceof SoknadDTO) {
                sykepengesoknad = toSykepengesoknad((SoknadDTO) soknad);
            } else if (soknad instanceof SykepengesoknadDTO) {
                sykepengesoknad = toSykepengesoknad((SykepengesoknadDTO) soknad);
            }

            if (sykepengesoknad != null) {
                saksbehandlingsService.behandleSoknad(sykepengesoknad);
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Uventet feil ved behandling av søknad", e);
            throw new RuntimeException("Uventet feil ved behandling av søknad");
        } finally {
            MDC.remove(CALL_ID);
        }
    }
}

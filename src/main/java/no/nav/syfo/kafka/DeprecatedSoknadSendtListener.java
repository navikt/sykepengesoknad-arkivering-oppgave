package no.nav.syfo.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.dto.Sykepengesoknad;
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
import static no.nav.syfo.kafka.mapper.DtoToSykepengesoknadMapper.konverter;

@Deprecated
@Component
@Slf4j
public class DeprecatedSoknadSendtListener {

    private final SaksbehandlingsService saksbehandlingsService;

    @Inject
    public DeprecatedSoknadSendtListener(SaksbehandlingsService saksbehandlingsService) {
        this.saksbehandlingsService = saksbehandlingsService;
    }

    @KafkaListener(topics = "privat-syfo-soknadSendt-v1", id = "deprecatedSoknadSendt", idIsGroup = false)
    public void listen(ConsumerRecord<String, SykepengesoknadDTO> cr, Acknowledgment acknowledgment) {
        log.debug("Melding mottatt på topic: {}, partisjon: {} med offsett: {}", cr.topic(), cr.partition(), cr.offset());

        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID, randomUUID().toString()));

            Sykepengesoknad sykepengesoknad = konverter(cr.value());
            saksbehandlingsService.behandleSoknad(sykepengesoknad);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Uventet feil ved behandling av søknad", e);
            throw new RuntimeException("Uventet feil ved behandling av søknad");
        } finally {
            MDC.remove(CALL_ID);
        }
    }
}

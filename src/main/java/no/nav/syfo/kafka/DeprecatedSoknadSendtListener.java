package no.nav.syfo.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.kafka.mapper.SoknadDtoToSykepengesoknadMapper;
import no.nav.syfo.kafka.soknad.dto.SoknadDTO;
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

@Component
@Slf4j
@Deprecated
public class DeprecatedSoknadSendtListener {

    private final SaksbehandlingsService saksbehandlingsService;

    @Inject
    public DeprecatedSoknadSendtListener(SaksbehandlingsService saksbehandlingsService) {
        this.saksbehandlingsService = saksbehandlingsService;
    }

    @KafkaListener(topics = "syfo-soknad-v1", id = "deprecatedSoknadSendt", idIsGroup = false, containerFactory = "deprecatedKafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, SoknadDTO> cr, Acknowledgment acknowledgment) {
        log.debug("Melding mottatt på topic: {}, partisjon: {} med offset: {}", cr.topic(), cr.partition(), cr.offset());

        try {
            MDC.put(CALL_ID, getLastHeaderByKeyAsString(cr.headers(), CALL_ID).orElseGet(randomUUID()::toString));

            saksbehandlingsService.behandleSoknad(SoknadDtoToSykepengesoknadMapper.INSTANCE.konverter(cr.value()));

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Uventet feil ved behandling av søknad", e);
            throw new RuntimeException("Uventet feil ved behandling av søknad");
        } finally {
            MDC.remove(CALL_ID);
        }
    }
}

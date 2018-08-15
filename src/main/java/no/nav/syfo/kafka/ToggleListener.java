package no.nav.syfo.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.config.unleash.Toggle;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static no.nav.syfo.config.unleash.FeatureToggle.SKAL_LESE_SOKNADER_FRA_KOE;

@Component
@Slf4j
public class ToggleListener {

    private KafkaListenerEndpointRegistry registry;
    private Toggle toggle;

    private boolean lytter = true;

    public ToggleListener(KafkaListenerEndpointRegistry registry, Toggle toggle) {
        this.registry = registry;
        this.toggle = toggle;
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void sjekkToggleOgOppdaterListener() {
        MessageListenerContainer soknadSendt = registry.getListenerContainer("soknadSendt");

        if (toggle.isEnabled(SKAL_LESE_SOKNADER_FRA_KOE)) {
            if (!lytter) {
                log.info("Vekker lytteren soknadSendt");
            }
            soknadSendt.resume();
            lytter = true;

        } else {
            if (lytter) {
                log.info("Pauser lytteren soknadSendt");
            }
            soknadSendt.pause();
            lytter = false;
        }
    }
}

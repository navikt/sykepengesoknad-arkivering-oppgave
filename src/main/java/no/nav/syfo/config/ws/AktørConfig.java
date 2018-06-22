package no.nav.syfo.config.ws;

import no.nav.syfo.consumer.util.ws.LogErrorHandler;
import no.nav.syfo.consumer.util.ws.WsClient;
import no.nav.tjeneste.virksomhet.aktoer.v2.AktoerV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class AktørConfig {

    @Bean
    @SuppressWarnings("unchecked")
    public AktoerV2 aktørV2(@Value("${virksomhet.aktor.v2.endpointurl}") String serviceUrl) {
        return new WsClient<AktoerV2>().createPort(serviceUrl, AktoerV2.class, Collections.singletonList(new LogErrorHandler()));
    }

}

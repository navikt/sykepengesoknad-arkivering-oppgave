package no.nav.syfo.config.ws

import no.nav.syfo.consumer.util.ws.LogErrorHandler
import no.nav.syfo.consumer.util.ws.createPort
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class ArbeidsfordelingConfig {
    @Bean
    @ConditionalOnProperty(value = ["mockWS"], havingValue = "false", matchIfMissing = true)
    @Primary
    fun arbeidsfordelingV1(@Value("\${virksomhet.arbeidsfordeling.v1.endpointurl}") serviceUrl: String,
                           @Value("\${ws.sts.enabled:true}") wsStsEnabled: Boolean): ArbeidsfordelingV1 {
        return createPort(serviceUrl = serviceUrl, handlers = listOf(LogErrorHandler()), wsStsEnabled = wsStsEnabled)
    }
}

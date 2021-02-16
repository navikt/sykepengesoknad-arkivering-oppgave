package no.nav.syfo.config.ws

import no.nav.syfo.consumer.util.ws.LogErrorHandler
import no.nav.syfo.consumer.util.ws.createPort
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class PersonConfig {

    @Bean
    @ConditionalOnProperty(value = ["mockWS"], havingValue = "false", matchIfMissing = true)
    @Primary
    fun personV3(
        @Value("\${virksomhet.person.v3.endpointurl}") serviceUrl: String,
        @Value("\${ws.sts.enabled:true}") wsStsEnabled: Boolean
    ): PersonV3 {
        return createPort(serviceUrl = serviceUrl, handlers = listOf(LogErrorHandler()), wsStsEnabled = wsStsEnabled, wsAddressingEnabled = false)
    }
}

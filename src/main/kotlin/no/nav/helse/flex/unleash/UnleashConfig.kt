package no.nav.helse.flex.unleash

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("remote")
class UnleashConfig(
    @Value("\${UNLEASH_SERVER_API_URL}") val apiUrl: String,
    @Value("\${UNLEASH_SERVER_API_TOKEN}") val apiToken: String,
) : DisposableBean {
    private val config: UnleashConfig =
        UnleashConfig
            .builder()
            .appName("sykepengesoknad-arkivering-oppgave")
            .unleashAPI("$apiUrl/api")
            .apiKey(apiToken)
            .synchronousFetchOnInitialisation(true)
            .build()
    private val defaultUnleash = DefaultUnleash(config)

    @Bean
    fun unleash(): Unleash = defaultUnleash

    override fun destroy() {
        // Spring trigger denne ved shutdown. Gjøres for å unngå at unleash fortsetter å gjøre kall ut
        defaultUnleash.shutdown()
    }
}

package no.nav.syfo.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Bean
    fun basicAuthRestTemplate(@Value("\${srvsyfogsak.username}") username: String,
                              @Value("\${srvsyfogsak.password}") password: String): RestTemplate {
        return RestTemplateBuilder()
                .basicAuthorization(username, password)
                .build()
    }

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}

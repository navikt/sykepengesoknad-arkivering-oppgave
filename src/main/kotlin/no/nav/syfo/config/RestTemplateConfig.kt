package no.nav.syfo.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets

@Configuration
class RestTemplateConfig {
    @Bean
    fun basicAuthRestTemplate(
            @Value("\${srvsyfogsak.username}") username: String,
            @Value("\${srvsyfogsak.password}") password: String
    ) = RestTemplateBuilder()
            .basicAuthentication(username, password)
            .build()

    @Bean
    fun restTemplateMedProxy(): RestTemplate {
        return RestTemplateBuilder()
                .additionalCustomizers(NaisProxyCustomizer())
                .build()
    }

    @Bean(name = ["plainTextUtf8RestTemplate"])
    fun plainTextUtf8RestTemplate(): RestTemplate {
        return RestTemplateBuilder()
                .messageConverters(StringHttpMessageConverter(StandardCharsets.UTF_8))
                .build()
    }
}

package no.nav.helse.flex.config

import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.client.RestTemplate

@Configuration
@EnableTransactionManagement
@EnableKafka
@EnableScheduling
class ApplicationConfig {
    @Bean
    fun pdfGenRestTemplate(): RestTemplate =
        RestTemplate().apply {
            messageConverters
                .mapNotNull { it as? AbstractJackson2HttpMessageConverter }
                .forEach { it.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) }
        }
}

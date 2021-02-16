package no.nav.syfo.config

import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
@EnableKafka
@Profile("test")
class KafkaTestConfig {

    @Bean
    fun producerFactory(kafkaProperties: KafkaProperties): ProducerFactory<String, SykepengesoknadDTO> {
        return DefaultKafkaProducerFactory(
            kafkaProperties.buildProducerProperties(),
            StringSerializer(),
            FunctionSerializer { byteArrayOf(1) }
        )
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, SykepengesoknadDTO>): KafkaTemplate<String, SykepengesoknadDTO> {
        return KafkaTemplate(producerFactory)
    }
}

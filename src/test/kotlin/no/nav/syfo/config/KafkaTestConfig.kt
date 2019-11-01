package no.nav.syfo.config

import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
@EnableKafka
class KafkaTestConfig {

    @Bean
    fun producerFactory(kafkaProperties: KafkaProperties): ProducerFactory<String, SoknadDTO> {
        return DefaultKafkaProducerFactory(
                kafkaProperties.buildProducerProperties(),
                StringSerializer(),
                FunctionSerializer { _ -> byteArrayOf(1) })
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, SoknadDTO>): KafkaTemplate<String, SoknadDTO> {
        return KafkaTemplate(producerFactory)
    }
}

package no.nav.syfo.config

import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.kafka.KafkaErrorHandler
import no.nav.syfo.kafka.soknad.deserializer.FunctionDeserializer
import no.nav.syfo.objectMapper
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import java.time.Duration

@Configuration
@EnableKafka
class KafkaConfig(private val kafkaErrorHandler: KafkaErrorHandler) {
    @Bean
    fun spreOppgaverConsumerFactory(properties: KafkaProperties): ConsumerFactory<String, OppgaveDTO> {
        return DefaultKafkaConsumerFactory(
            properties.buildConsumerProperties(),
            StringDeserializer(),
            FunctionDeserializer { bytes -> objectMapper.readValue(bytes, OppgaveDTO::class.java) }
        )
    }

    @Bean
    fun spreOppgaverContainerFactory(consumerFactory: ConsumerFactory<String, OppgaveDTO>): ConcurrentKafkaListenerContainerFactory<String, OppgaveDTO> =
        ConcurrentKafkaListenerContainerFactory<String, OppgaveDTO>().apply {
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
            setErrorHandler(kafkaErrorHandler)
            this.containerProperties.authorizationExceptionRetryInterval = Duration.ofSeconds(2)
            this.consumerFactory = consumerFactory
        }
}

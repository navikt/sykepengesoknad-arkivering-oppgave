package no.nav.syfo.config

import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.KafkaErrorHandler
import no.nav.syfo.kafka.felles.DeprecatedSykepengesoknadDTO
import no.nav.syfo.kafka.soknad.deserializer.FunctionDeserializer
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer
import no.nav.syfo.objectMapper
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties
import java.time.Duration

@Configuration
@EnableKafka
class KafkaConfig(private val kafkaErrorHandler: KafkaErrorHandler, private val properties: KafkaProperties) {
    @Bean
    fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, DeprecatedSykepengesoknadDTO>): ConcurrentKafkaListenerContainerFactory<String, DeprecatedSykepengesoknadDTO> =
        ConcurrentKafkaListenerContainerFactory<String, DeprecatedSykepengesoknadDTO>().apply {
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
            setErrorHandler(kafkaErrorHandler)
            this.containerProperties.authorizationExceptionRetryInterval = Duration.ofSeconds(2)
            this.consumerFactory = consumerFactory
        }

    @Bean
    fun consumerFactory(properties: KafkaProperties): ConsumerFactory<String, DeprecatedSykepengesoknadDTO> {
        return DefaultKafkaConsumerFactory(
            properties.buildConsumerProperties(),
            StringDeserializer(),
            FunctionDeserializer { bytes -> objectMapper.readValue(bytes, DeprecatedSykepengesoknadDTO::class.java) }
        )
    }

    @Bean
    fun rebehandlingConsumerFactory(properties: KafkaProperties): ConsumerFactory<String, Sykepengesoknad> {
        return DefaultKafkaConsumerFactory(
            properties.buildConsumerProperties(),
            StringDeserializer(),
            FunctionDeserializer { bytes -> objectMapper.readValue(bytes, Sykepengesoknad::class.java) }
        )
    }

    @Bean
    fun rebehandlingContainerFactory(consumerFactory: ConsumerFactory<String, Sykepengesoknad>): ConcurrentKafkaListenerContainerFactory<String, Sykepengesoknad> =
        ConcurrentKafkaListenerContainerFactory<String, Sykepengesoknad>().apply {
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
            setErrorHandler(kafkaErrorHandler)
            this.containerProperties.authorizationExceptionRetryInterval = Duration.ofSeconds(2)
            this.consumerFactory = consumerFactory
        }

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

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Sykepengesoknad> = KafkaTemplate(
        DefaultKafkaProducerFactory(
            properties.buildProducerProperties(),
            StringSerializer(),
            FunctionSerializer<Sykepengesoknad>(objectMapper::writeValueAsBytes)
        )
    )
}

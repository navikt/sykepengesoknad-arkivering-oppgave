package no.nav.syfo.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.KafkaErrorHandler
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.soknad.deserializer.MultiFunctionDeserializer
import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SykepengesoknadArbeidsledigDTO
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.AbstractMessageListenerContainer
import java.io.IOException
import java.util.function.BiFunction

@Configuration
@EnableKafka
class KafkaConfig {

    private val objectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)

    @Bean
    fun kafkaListenerContainerFactory(
            consumerFactory: ConsumerFactory<String, Soknad>,
            kafkaErrorHandler: KafkaErrorHandler): ConcurrentKafkaListenerContainerFactory<String, Soknad> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Soknad>()
        factory.containerProperties.ackMode = AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.setErrorHandler(kafkaErrorHandler)
        factory.consumerFactory = consumerFactory
        return factory
    }

    @Bean
    fun consumerFactory(properties: KafkaProperties): ConsumerFactory<String, Soknad>  {
        return DefaultKafkaConsumerFactory(
            properties.buildConsumerProperties(),
            StringDeserializer(),
            MultiFunctionDeserializer(mapOf(
                    "SYKEPENGESOKNAD" to BiFunction { _, bytes -> objectMapper.readValue(bytes, SykepengesoknadDTO::class.java) },
                    "SOKNAD" to BiFunction { _, bytes -> objectMapper.readValue(bytes, SoknadDTO::class.java) })))
    }

    @Bean
    fun kafkaListenerContainerFactoryArbeidsledig(
        consumerFactory: ConsumerFactory<String, SykepengesoknadArbeidsledigDTO>,
        kafkaErrorHandler: KafkaErrorHandler): ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadArbeidsledigDTO> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadArbeidsledigDTO>()
        factory.containerProperties.ackMode = AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.setErrorHandler(kafkaErrorHandler)
        factory.consumerFactory = consumerFactory
        return factory
    }

    @Bean
    fun consumerFactoryArbeidsledig(properties: KafkaProperties): ConsumerFactory<String, SykepengesoknadArbeidsledigDTO>  {
        return DefaultKafkaConsumerFactory(
            properties.buildConsumerProperties(),
            StringDeserializer(),
            MultiFunctionDeserializer(emptyMap()
            ) { bytes ->
                try {
                    objectMapper.readValue(bytes, SykepengesoknadArbeidsledigDTO::class.java)
                } catch (e: IOException) {
                    throw RuntimeException("Feil ved konvertering av bytes til SykepengesoknadArbeidsledigDTO", e)
                }
            })
    }

    @Bean
    fun kafkaListenerContainerFactoryRebehandling(
        consumerFactory: ConsumerFactory<String, Sykepengesoknad>,
        kafkaErrorHandler: KafkaErrorHandler): ConcurrentKafkaListenerContainerFactory<String, Sykepengesoknad> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Sykepengesoknad>()
        factory.containerProperties.ackMode = AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.setErrorHandler(kafkaErrorHandler)
        factory.consumerFactory = consumerFactory
        return factory
    }

    @Bean
    fun consumerFactoryRebehandling(properties: KafkaProperties): ConsumerFactory<String, Sykepengesoknad>  {
        return DefaultKafkaConsumerFactory(
            properties.buildConsumerProperties(),
            StringDeserializer(),
            MultiFunctionDeserializer(emptyMap()
            ) { bytes ->
                try {
                    objectMapper.readValue(bytes, Sykepengesoknad::class.java)
                } catch (e: IOException) {
                    throw RuntimeException("Feil ved konvertering av bytes til Sykepengesoknad", e)
                }
            })
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Sykepengesoknad>): KafkaTemplate<String, Sykepengesoknad> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun producerFactory(properties: KafkaProperties): ProducerFactory<String, Sykepengesoknad> {
        return DefaultKafkaProducerFactory(properties.buildProducerProperties(),
            StringSerializer(),
            FunctionSerializer { sykepengesoknad -> objectMapper.writeValueAsBytes(sykepengesoknad) }
        )
    }
}



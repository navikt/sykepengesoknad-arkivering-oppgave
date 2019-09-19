package no.nav.syfo.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.syfo.kafka.KafkaErrorHandler
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.soknad.deserializer.MultiFunctionDeserializer
import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SykepengesoknadArbeidsledigDTO
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.AbstractMessageListenerContainer
import java.io.IOException
import java.util.function.BiFunction

@Configuration
@EnableKafka
class KafkaConfig {

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
        val objectMapper = ObjectMapper()
                .registerModule(JavaTimeModule())
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)

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
        val objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
        return DefaultKafkaConsumerFactory(
            properties.buildConsumerProperties(),
            StringDeserializer(),
            MultiFunctionDeserializer(emptyMap()
            ) { bytes ->
                try {
                    objectMapper.readValue<SykepengesoknadArbeidsledigDTO>(bytes, SykepengesoknadArbeidsledigDTO::class.java)
                } catch (e: IOException) {
                    throw RuntimeException("Feil ved konvertering av bytes til SykepengesoknadArbeidsledigDTO", e)
                }
            })
    }
}



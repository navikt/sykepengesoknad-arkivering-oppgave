package no.nav.syfo.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.KafkaErrorHandler
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.soknad.deserializer.MultiFunctionDeserializer
import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.soknad.serializer.FunctionSerializer
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SykepengesoknadArbeidsledigDTO
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.AbstractMessageListenerContainer
import java.util.function.BiFunction

@Configuration
@EnableKafka
class KafkaConfig(private val kafkaErrorHandler: KafkaErrorHandler, private val properties: KafkaProperties) {

    private companion object {
        private val objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .registerKotlinModule()
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
    }

    @Bean
    fun soknadContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Soknad> =
        containerFactory(soknadDeserializer())

    @Bean
    fun arbeidsledigContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, SykepengesoknadArbeidsledigDTO> =
        containerFactory(deserializer())

    @Bean
    fun rebehandlingContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Sykepengesoknad> =
        containerFactory(deserializer())

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Sykepengesoknad> = KafkaTemplate(
        DefaultKafkaProducerFactory(
            properties.buildProducerProperties(),
            StringSerializer(),
            FunctionSerializer<Sykepengesoknad>(objectMapper::writeValueAsBytes)
        )
    )

    private inline fun <reified T> containerFactory(deserializer: MultiFunctionDeserializer<T>) =
        ConcurrentKafkaListenerContainerFactory<String, T>().apply {
            containerProperties.ackMode = AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE
            containerProperties.setErrorHandler(kafkaErrorHandler)
            consumerFactory = consumerFactory(deserializer)
        }

    private inline fun <reified T> consumerFactory(valueDeserializer: Deserializer<T>) =
        DefaultKafkaConsumerFactory(
            properties.buildConsumerProperties(),
            StringDeserializer(),
            valueDeserializer
        )

    private inline fun <reified T> deserializer() = MultiFunctionDeserializer<T>(emptyMap(), objectMapper::readValue)

    fun soknadDeserializer() = MultiFunctionDeserializer<Soknad>(
        mapOf(
            "SYKEPENGESOKNAD" to BiFunction { _, bytes -> objectMapper.readValue<SykepengesoknadDTO>(bytes) },
            "SOKNAD" to BiFunction { _, bytes -> objectMapper.readValue<SoknadDTO>(bytes) }
        )
    )
}

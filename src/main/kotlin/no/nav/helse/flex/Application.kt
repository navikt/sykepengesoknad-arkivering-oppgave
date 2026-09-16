package no.nav.helse.flex

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.resilience.annotation.EnableResilientMethods
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

const val BEHANDLINGSTIDSPUNKT = "behandlingstidspunkt"

@SpringBootApplication
@EnableResilientMethods
@EnableJwtTokenValidation
class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

val objectMapper: ObjectMapper =
    JsonMapper
        .builder()
        .addModule(kotlinModule())
        .build()

fun Any.serialisertTilString(): String = objectMapper.writeValueAsString(this)

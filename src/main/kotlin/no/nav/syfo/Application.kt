package no.nav.syfo

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.transaction.annotation.EnableTransactionManagement

const val AAD = "aad"
const val BEHANDLINGSTIDSPUNKT = "behandlingstidspunkt"

@SpringBootApplication
@EnableTransactionManagement
@EnableRetry
@EnableJwtTokenValidation
class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}

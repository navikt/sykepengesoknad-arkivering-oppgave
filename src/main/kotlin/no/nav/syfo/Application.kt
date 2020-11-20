package no.nav.syfo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.transaction.annotation.EnableTransactionManagement

const val AZUREAD = "azuread"
const val BEHANDLINGSTIDSPUNKT = "behandlingstidspunkt"

@SpringBootApplication
@EnableTransactionManagement
@EnableRetry
class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}

package no.nav.syfo

import org.h2.tools.Server
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import java.sql.SQLException
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@SpringBootApplication
class TestApplication {

    @Bean
    @Profile("local")
    fun server(): Server {
        try {
            return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "8082").start()
        } catch (e: SQLException) {
            log().error("Klarte ikke starte databasekobling", e)
            throw RuntimeException("Klarte ikke starte databasekobling", e)
        }

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            setTrustAllCerts()
            SpringApplication.run(TestApplication::class.java, *args)
        }

        private fun setTrustAllCerts() {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate>? {
                    return null
                }

                override fun checkClientTrusted(
                        certs: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                override fun checkServerTrusted(
                        certs: Array<java.security.cert.X509Certificate>, authType: String) {
                }
            })

            // Install the all-trusting trust manager
            try {
                val sc = SSLContext.getInstance("SSL")
                sc.init(null, trustAllCerts, java.security.SecureRandom())
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            } catch (e: Exception) {
            }

        }
    }
}

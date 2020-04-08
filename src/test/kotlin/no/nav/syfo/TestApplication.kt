package no.nav.syfo

import org.h2.tools.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Profile
import java.sql.SQLException

@SpringBootApplication
class TestApplication{

    @Profile("local")
    fun server(): Server {
        try {
            return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "8082").start()
        } catch (e: SQLException) {
            log().error("Klarte ikke starte databasekobling", e)
            throw RuntimeException("Klarte ikke starte databasekobling", e)
        }

    }
}

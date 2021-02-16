package no.nav.syfo.localconfig

import org.h2.tools.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.sql.SQLException

@Configuration
@EnableTransactionManagement
class TestApplicationConfig {

    @Bean
    @Profile("local")
    @Throws(SQLException::class)
    fun server(@Value("\${h2.tcp.port:8182}") port: String): Server {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", port).start()
    }
}

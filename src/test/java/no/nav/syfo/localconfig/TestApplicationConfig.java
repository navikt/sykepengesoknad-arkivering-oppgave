package no.nav.syfo.localconfig;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.sql.SQLException;

@Configuration
@EnableTransactionManagement
public class TestApplicationConfig {

    @Bean
    @Profile("local")
    public Server server(@Value("${h2.tcp.port:8182}") String port) throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", port).start();
    }
}

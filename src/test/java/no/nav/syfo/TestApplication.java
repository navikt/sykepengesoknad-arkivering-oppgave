package no.nav.syfo;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.sql.SQLException;

@Slf4j
@SpringBootApplication
public class TestApplication {
    public static void main(String[] args) {
        setTrustAllCerts();
        SpringApplication.run(TestApplication.class, args);
    }

    @Bean
    @Profile("local")
    public Server server() {
        try {
            return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "8082").start();
        } catch (SQLException e) {
            log.error("Klarte ikke starte databasekobling", e);
            throw new RuntimeException("Klarte ikke starte databasekobling", e);
        }
    }

    private static void setTrustAllCerts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }
}

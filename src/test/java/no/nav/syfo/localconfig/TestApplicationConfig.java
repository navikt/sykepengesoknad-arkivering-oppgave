package no.nav.syfo.localconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@TestPropertySource(locations = "classpath:application-test.yaml")
@EnableTransactionManagement
public class TestApplicationConfig {

    public TestApplicationConfig() {}

}

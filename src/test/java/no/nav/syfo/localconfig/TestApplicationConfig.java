package no.nav.syfo.localconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

@Configuration
@TestPropertySource(locations = "classpath:application-test.yaml")
public class TestApplicationConfig {

    public TestApplicationConfig() {}
}

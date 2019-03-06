package no.nav.syfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class Application {

    public static String CALL_ID = "Nav-Callid";
    public static String INTERN = "intern";
    public static String AZUREAD = "azuread";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

package no.nav.syfo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Toggle {

    public static boolean endepunkter;

    public Toggle(@Value("${TOGGLE_ENDEPUNKT_FOR_INNKOMMENDE_SOKNAD:false}") boolean endepunkter) {
        Toggle.endepunkter = endepunkter;
    }
}
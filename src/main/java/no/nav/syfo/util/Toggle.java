package no.nav.syfo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Toggle {

    public static boolean endepunkter;

    public Toggle(@Value("${toggle.endepunkt-for-innkommende-soknad:false}") boolean endepunkter) {
        Toggle.endepunkter = endepunkter;
    }
}
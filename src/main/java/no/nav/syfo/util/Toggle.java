package no.nav.syfo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Toggle {

    public static boolean endepunkter;
    public static boolean skipSaksbehandling;

    public Toggle(
            @Value("${toggle.endepunkt.innkommende.soknad:false}") boolean endepunkter,
            @Value("${toggle.skip.saksbehandling:true}") boolean skipSaksbehandling) {
        Toggle.endepunkter = endepunkter;
        Toggle.skipSaksbehandling = skipSaksbehandling;
    }
}
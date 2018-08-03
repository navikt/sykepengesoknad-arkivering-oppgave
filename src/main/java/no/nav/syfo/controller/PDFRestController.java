package no.nav.syfo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.nav.syfo.domain.Soknad;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;


import static org.springframework.http.HttpStatus.OK;

@Controller
public class PDFRestController {


    private static String pdfPath;
    private static final Logger LOG = LoggerFactory.getLogger(PDFRestController.class);

    public PDFRestController(@Value("${pdfgen.url}") String pdfPath) {
        PDFRestController.pdfPath = pdfPath;
    }

    public static byte[] getPDF(Soknad soknad) {
        final String url = pdfPath + "/syfosoknader/sykepengerutland";

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<Soknad> entity = new HttpEntity<>(soknad, headers);

        final ResponseEntity<byte[]> result = new RestTemplate().exchange(url, HttpMethod.POST, entity, byte[].class);

        if (result.getStatusCode() != OK) {
            throw new RuntimeException("getPDF feiler med HTTP-" + result.getStatusCode() + " for søknad om utenlandsopphold med id: " + soknad.soknadsId);
        }

        return result.getBody();
    }
}

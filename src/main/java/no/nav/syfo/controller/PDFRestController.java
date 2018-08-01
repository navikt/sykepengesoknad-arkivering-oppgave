package no.nav.syfo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.nav.syfo.domain.Soknad;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


import static org.springframework.http.HttpStatus.OK;

public class PDFRestController {

    @Value("${pdfgen.url}")
    private static String pdfPath;
    private static final Logger LOG = LoggerFactory.getLogger(PDFRestController.class);

    public static ResponseEntity<byte[]> getPDF(Soknad soknad) {
        final String url = "http://e34apvl00253.devillo.no:8080/api/v1/genpdf" + "/syfosoknader/sykepengerutland";

        System.out.println("pdfPath: " + pdfPath);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<Soknad> entity = new HttpEntity<>(soknad, headers);

        final ResponseEntity<byte[]> result = new RestTemplate().exchange(url, HttpMethod.POST, entity, byte[].class);


        if (result.getStatusCode() != OK) {
            throw new RuntimeException("getPDF feiler med HTTP-" + result.getStatusCode() + " for søknad om utenlandsopphold med id: " + soknad.soknadsId);
        }

        LOG.info("Oppretter søknad for å beholde sykepenger under utenlandsopphold med id: " + soknad.soknadsId);

        // who would win? a fully fledged debugger or a couple of printy boys
        System.out.println("entity" + entity.toString());

        return result;
    }
}

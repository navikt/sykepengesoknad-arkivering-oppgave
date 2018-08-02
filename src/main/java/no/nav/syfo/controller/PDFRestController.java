package no.nav.syfo.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.dto.PDFTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.nav.syfo.domain.Soknad;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;


import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Controller
public class PDFRestController {


    private String pdfPath;
    private static final Logger LOG = LoggerFactory.getLogger(PDFRestController.class);

    public PDFRestController(@Value("${pdfgen.url}") String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public byte[] getPDF(Soknad soknad, PDFTemplate template) {


        final String url = this.pdfPath + "/v1/genpdf/syfosoknader/" + template;

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<Soknad> entity = new HttpEntity<>(soknad, headers);

        log.info(entity.toString());
        final ResponseEntity<byte[]> result = new RestTemplate().exchange(url, HttpMethod.POST, entity, byte[].class);

        if (result.getStatusCode() != OK) {
            throw new RuntimeException("getPDF feiler med HTTP-" + result.getStatusCode() + " for søknad om utenlandsopphold med id: " + soknad.soknadsId);
        }

        return result.getBody();
    }
}

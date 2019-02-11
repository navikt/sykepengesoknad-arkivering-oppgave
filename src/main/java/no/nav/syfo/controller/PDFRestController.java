package no.nav.syfo.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.PdfSoknad;
import no.nav.syfo.domain.dto.PDFTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Controller
public class PDFRestController {
    private RestTemplate restTemplate;

    public PDFRestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] getPDF(PdfSoknad soknad, PDFTemplate template) {
        final String url = "http://pdf-gen.default/api/v1/genpdf/syfosoknader/" + template;

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final HttpEntity<PdfSoknad> entity = new HttpEntity<>(soknad, headers);

        final ResponseEntity<byte[]> result = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);

        if (result.getStatusCode() != OK) {
            throw new RuntimeException("getPDF feiler med HTTP-" + result.getStatusCode() + " for søknad om utenlandsopphold med id: " + soknad.getSoknadsId());
        }

        return result.getBody();
    }
}

package no.nav.syfo.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Periode;

import java.io.IOException;

@Slf4j
public class PeriodeMapper {

    private static ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static Periode jsonTilPeriode(final String json) {
        try {
            final Periode periode = objectMapper.readValue(json, Periode.class);
            if (periode.getTom() == null || periode.getFom() == null
                    || periode.getFom().isAfter(periode.getTom())) {
                throw new IllegalArgumentException();
            }
            return periode;
        } catch (JsonParseException | JsonMappingException exception) {
            throw new IllegalArgumentException(exception);
        } catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
    }
}

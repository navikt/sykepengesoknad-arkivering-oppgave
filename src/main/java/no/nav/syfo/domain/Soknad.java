package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class Soknad {
    public String aktørId;
}

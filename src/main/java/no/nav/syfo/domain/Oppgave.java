package no.nav.syfo.domain;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@Builder
public class Oppgave {
    String beskrivelse;
}


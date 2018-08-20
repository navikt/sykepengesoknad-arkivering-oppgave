package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@Data
@Builder
@Getter
public class Innsending {
    String innsendingsId;
    String ressursId;
    String aktorId;
    String saksId;
    String journalpostId;
    String oppgaveId;
    LocalDate behandlet;
}

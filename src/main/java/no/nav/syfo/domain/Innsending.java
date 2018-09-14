package no.nav.syfo.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
@Getter
public class Innsending {
    @NonNull String innsendingsId;
    @NonNull String ressursId;
    String aktorId;
    String saksId;
    String journalpostId;
    String oppgaveId;
    LocalDate behandlet;
}

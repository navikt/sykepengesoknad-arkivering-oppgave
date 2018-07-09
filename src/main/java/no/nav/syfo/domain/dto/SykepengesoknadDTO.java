package no.nav.syfo.domain.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SykepengesoknadDTO {

    String id;
    String sykmeldingId;
    String aktorId;
    SoknadstypeDTO soknadstypeDTO;
    String status;
    String fom;
    String tom;
    String opprettetDato;
    List<SporsmalDTO> sporsmalDTO;

}

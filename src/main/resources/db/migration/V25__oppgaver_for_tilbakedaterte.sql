CREATE TABLE OPPGAVER_FOR_TILBAKEDATERTE
(
    id                   VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    sykepengesoknad_uuid VARCHAR                  NOT NULL,
    sykmelding_uuid      VARCHAR                  NOT NULL,
    oppgave_id           VARCHAR                  NOT NULL,
    status               VARCHAR                  NOT NULL,
    opprettet            TIMESTAMP WITH TIME ZONE NOT NULL,
    oppdatert            TIMESTAMP WITH TIME ZONE
);

CREATE INDEX oppgaver_tilbakedatert_sm_idx ON OPPGAVER_FOR_TILBAKEDATERTE (sykmelding_uuid);
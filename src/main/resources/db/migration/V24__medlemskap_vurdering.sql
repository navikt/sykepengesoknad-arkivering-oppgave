CREATE TABLE MEDLEMSKAP_VURDERING
(
    id                  VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    fnr                 VARCHAR(11) NOT NULL,
    sykepengesoknad_id  VARCHAR     NOT NULL,
    fom                 DATE        NOT NULL,
    tom                 DATE        NOT NULL,
    inngaende_vurdering VARCHAR     NOT NULL,
    vurdering_id        VARCHAR,
    endelig_vurdering   VARCHAR
);

CREATE INDEX medlemskap_vurdering_idx ON medlemskap_vurdering(fnr, sykepengesoknad_id);
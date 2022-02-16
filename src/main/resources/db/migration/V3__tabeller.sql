DROP TABLE INNSENDING;
DROP TABLE OPPGAVESTYRING;

CREATE TABLE INNSENDING
(
    id                 VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    sykepengesoknad_id VARCHAR(100) UNIQUE NOT NULL,
    journalpost_id     VARCHAR(20),
    oppgave_id         VARCHAR(20),
    behandlet          TIMESTAMP WITH TIME ZONE
);

CREATE TABLE OPPGAVESTYRING
(
    id                 VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    sykepengesoknad_id VARCHAR(100) UNIQUE      NOT NULL,
    status             VARCHAR(20)              NOT NULL,
    opprettet          TIMESTAMP WITH TIME ZONE NOT NULL,
    modifisert         TIMESTAMP WITH TIME ZONE NOT NULL,
    timeout            TIMESTAMP WITH TIME ZONE,
    avstemt            BOOLEAN                  NOT NULL
);

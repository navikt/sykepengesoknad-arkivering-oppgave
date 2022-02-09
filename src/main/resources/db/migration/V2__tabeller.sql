CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE INNSENDING
(
    id                 VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    sykepengesoknad_id VARCHAR(100) NOT NULL,
    journalpost_id     VARCHAR(20),
    oppgave_id         VARCHAR(20),
    behandlet          TIMESTAMP WITH TIME ZONE,
    soknad_fom         TIMESTAMP WITH TIME ZONE,
    soknad_tom         TIMESTAMP WITH TIME ZONE
);

CREATE TABLE OPPGAVESTYRING
(
    id                 VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    sykepengesoknad_id VARCHAR(100)             NOT NULL,
    status             VARCHAR(20)              NOT NULL,
    opprettet          TIMESTAMP WITH TIME ZONE NOT NULL,
    modifisert         TIMESTAMP WITH TIME ZONE NOT NULL,
    timeout            TIMESTAMP WITH TIME ZONE,
    avstemt            BOOLEAN            NOT NULL
);

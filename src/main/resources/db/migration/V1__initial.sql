DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'cloudsqliamuser')
        THEN
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO cloudsqliamuser;
        END IF;
    END
$$;

CREATE TABLE INNSENDING
(
    id                 VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    sykepengesoknad_id VARCHAR(100)             NOT NULL,
    journalpost_id     VARCHAR(20)              NOT NULL,
    oppgave_id         VARCHAR(20)              NOT NULL,
    behandlet          TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE OPPGAVESTYRING
(
    id                 VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    sykepengesoknad_id VARCHAR(100)             NOT NULL,
    status             VARCHAR(20)              NOT NULL,
    opprettet          TIMESTAMP WITH TIME ZONE NOT NULL,
    modifisert         TIMESTAMP WITH TIME ZONE NOT NULL,
    timeout            TIMESTAMP WITH TIME ZONE,
    avstemt            NUMERIC(1, 0)            NOT NULL
);

CREATE TABLE OPPGAVESTYRING_LOG
(
    id                 VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    oppgavestyring_id  INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) NOT NULL,
    sykepengesoknad_id VARCHAR(100)                                                       NOT NULL,
    oppdateringstype   VARCHAR(100)                                                       NOT NULL,
    opprettet_dato     DATE                                                               NOT NULL,
    timeout            TIMESTAMP WITH TIME ZONE
);

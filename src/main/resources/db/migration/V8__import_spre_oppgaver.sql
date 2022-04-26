CREATE TABLE OPPGAVEFORDELING
(
    sykepengesoknad_id VARCHAR(36) PRIMARY KEY,
    status             VARCHAR(20)              NOT NULL,
    timeout            TIMESTAMP WITH TIME ZONE NULL
);

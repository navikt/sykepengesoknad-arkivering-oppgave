CREATE TABLE UTSATTE_OPPGAVER (
    id VARCHAR(100) PRIMARY KEY,
    innsending_uuid VARCHAR(100) NOT NULL,
    oppgave_foresporsel BLOB NOT NULL,
    CONSTRAINT foreign_key_innsending_uuid FOREIGN KEY (innsending_uuid) REFERENCES INNSENDING(innsending_uuid)
)

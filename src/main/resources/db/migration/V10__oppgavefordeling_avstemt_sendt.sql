ALTER TABLE oppgavefordeling
    ADD COLUMN avstemt   BOOLEAN DEFAULT FALSE,
    ADD COLUMN sendt_nav TIMESTAMP WITH TIME ZONE NULL;

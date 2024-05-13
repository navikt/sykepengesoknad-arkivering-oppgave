DROP INDEX medlemskap_vurdering_idx;
CREATE INDEX medlemskap_vurdering_idx ON medlemskap_vurdering (sykepengesoknad_id);

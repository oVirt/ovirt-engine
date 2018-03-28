ALTER TABLE network ALTER COLUMN label SET DEFAULT NULL;
UPDATE network SET label = NULL WHERE label = '';


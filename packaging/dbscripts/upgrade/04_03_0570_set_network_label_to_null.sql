-- set network table label column to null since it was
--changed by mistake to empty string in
--<prefix>_default_all_search_engine_string_fields_to_not_null.sql
ALTER TABLE network ALTER COLUMN label SET DEFAULT NULL;
UPDATE network SET label = NULL WHERE label = '';


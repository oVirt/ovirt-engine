ALTER TABLE users ALTER COLUMN options DROP DEFAULT;

SELECT fn_db_update_column_to_jsonb_compatible_values('users', 'options', '{}');

SELECT fn_db_change_column_type('users','options','text', 'jsonb USING options::jsonb');

ALTER TABLE users ALTER COLUMN options SET DEFAULT '{}'::jsonb;

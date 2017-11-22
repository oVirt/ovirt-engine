SELECT fn_db_add_column('providers', 'is_unmanaged', 'BOOLEAN NOT NULL DEFAULT FALSE');
ALTER TABLE providers ALTER COLUMN url DROP NOT NULL;
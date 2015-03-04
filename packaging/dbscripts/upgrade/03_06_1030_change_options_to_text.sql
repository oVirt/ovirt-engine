SELECT fn_db_add_column('fence_agents', 'encrypt_options', 'BOOLEAN NOT NULL DEFAULT FALSE');
SELECT fn_db_change_column_type('fence_agents', 'options', 'VARCHAR', 'TEXT');

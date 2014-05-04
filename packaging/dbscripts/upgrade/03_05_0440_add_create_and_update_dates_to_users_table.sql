SELECT fn_db_add_column('users', '_create_date', 'timestamp with time zone DEFAULT CURRENT_TIMESTAMP');
SELECT fn_db_add_column('users', '_update_date', 'timestamp with time zone');

select fn_db_add_column('vm_device', '_create_date', 'timestamp with time zone DEFAULT current_timestamp');
select fn_db_add_column('vm_device', '_update_date', 'timestamp with time zone');

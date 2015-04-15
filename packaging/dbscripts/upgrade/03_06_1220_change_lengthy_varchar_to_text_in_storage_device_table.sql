SELECT fn_db_change_column_type('storage_device', 'name', 'varchar', 'text');
SELECT fn_db_change_column_type('storage_device', 'description', 'varchar', 'text');
SELECT fn_db_change_column_type('storage_device', 'device_path', 'varchar', 'text');
SELECT fn_db_change_column_type('storage_device', 'mount_point', 'varchar', 'text');
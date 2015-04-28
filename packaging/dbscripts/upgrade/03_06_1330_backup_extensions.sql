select fn_db_add_column('engine_backup_log', 'fqdn',  'varchar(255)');
select fn_db_add_column('engine_backup_log', 'log_path', 'text');
select fn_db_rename_column('engine_backup_log', 'db_name', 'scope');

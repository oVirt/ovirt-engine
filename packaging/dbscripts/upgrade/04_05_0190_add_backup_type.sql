SELECT fn_db_add_column('vm_backups', 'backup_type', 'VARCHAR(50) NOT NULL DEFAULT ''hybrid''');

UPDATE vm_backups
SET backup_type = CASE
    WHEN is_live_backup IS TRUE THEN 'live'
    WHEN is_live_backup IS FALSE THEN 'cold'
END;

SELECT fn_db_drop_column('vm_backups', 'is_live_backup');

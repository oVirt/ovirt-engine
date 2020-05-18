SELECT fn_db_create_constraint('vm_backup_disk_map',
                               'fk_vm_backup_disk_map_vm_backups',
                               'FOREIGN KEY (backup_id) REFERENCES vm_backups(backup_id) ON DELETE CASCADE');

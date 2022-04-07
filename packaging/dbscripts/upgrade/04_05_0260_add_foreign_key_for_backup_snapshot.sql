SELECT fn_db_create_constraint('vm_backups', 'fk_backups_snapshots', 'FOREIGN KEY (snapshot_id) REFERENCES snapshots(snapshot_id) ON DELETE SET DEFAULT');


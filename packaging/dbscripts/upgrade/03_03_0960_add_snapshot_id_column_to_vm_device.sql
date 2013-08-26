select fn_db_add_column('vm_device', 'snapshot_id', 'UUID REFERENCES SNAPSHOTS(snapshot_id) ON DELETE CASCADE DEFAULT NULL');

SELECT fn_db_create_index('idx_snapshots_memory_metadata_disk_id', 'snapshots', 'memory_metadata_disk_id', '', false);
SELECT fn_db_create_index('idx_snapshots_memory_dump_disk_id', 'snapshots', 'memory_dump_disk_id', '', false);


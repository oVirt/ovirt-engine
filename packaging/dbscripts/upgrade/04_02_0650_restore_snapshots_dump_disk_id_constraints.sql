UPDATE snapshots
SET memory_dump_disk_id = NULL
WHERE memory_dump_disk_id NOT IN (SELECT disk_id FROM base_disks);

UPDATE snapshots
SET memory_metadata_disk_id = NULL
WHERE memory_metadata_disk_id NOT IN (SELECT disk_id FROM base_disks);

SELECT fn_db_create_constraint('snapshots',
                               'fk_snapshots_dump_disk_id',
                               'FOREIGN KEY (memory_dump_disk_id) REFERENCES base_disks(disk_id) ON DELETE SET NULL');

SELECT fn_db_create_constraint('snapshots',
                               'fk_snapshots_metadata_disk_id',
                               'FOREIGN KEY (memory_metadata_disk_id) REFERENCES base_disks(disk_id) ON DELETE SET NULL');

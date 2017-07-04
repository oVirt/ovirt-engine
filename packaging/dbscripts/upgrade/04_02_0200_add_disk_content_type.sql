SELECT fn_db_add_column('base_disks', 'disk_content_type', 'SMALLINT NOT NULL DEFAULT 0');

SELECT fn_db_create_index('idx_disk_content_type', 'base_disks', 'disk_content_type', '', false);

-- Set content type for OVF store disks
UPDATE base_disks bd SET disk_content_type = 1 WHERE
EXISTS (SELECT ovf_disk_id FROM storage_domains_ovf_info ovf WHERE ovf.ovf_disk_id = bd.disk_id);

-- Set content type for memory dump disks
UPDATE base_disks bd SET disk_content_type = 2 WHERE
EXISTS (SELECT memory_dump_disk_id from SNAPSHOTS WHERE bd.disk_id  = memory_dump_disk_id);

-- Set content type for memory metadata disks
UPDATE base_disks bd SET disk_content_type = 3 WHERE
EXISTS (SELECT memory_metadata_disk_id from SNAPSHOTS WHERE bd.disk_id = memory_metadata_disk_id);

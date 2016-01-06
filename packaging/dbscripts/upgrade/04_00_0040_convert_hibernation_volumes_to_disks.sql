-- add memory disk columns to the snapshots table
SELECT fn_db_add_column('snapshots', 'memory_metadata_disk_id', 'uuid default NULL');
SELECT fn_db_add_column('snapshots', 'memory_dump_disk_id', 'uuid default NULL');

-- add foreign key constraints to disks
SELECT fn_db_create_constraint('snapshots', 'fk_snapshots_metadata_disk_id', 'FOREIGN KEY (memory_metadata_disk_id) REFERENCES base_disks(disk_id) ON DELETE SET NULL');
SELECT fn_db_create_constraint('snapshots', 'fk_snapshots_dump_disk_id', 'FOREIGN KEY (memory_dump_disk_id) REFERENCES base_disks(disk_id) ON DELETE SET NULL');

-- meta data image
INSERT INTO images(
    image_guid,
    creation_date,
    size,
    it_guid,
    imagestatus,
    volume_type,
    volume_format,
    image_group_id
    )
SELECT
    cast(split_part(memory_volume, ',', 6) as uuid),
    'now',
    10240,
    '00000000-0000-0000-0000-000000000000',
    1, -- ok
    2, -- sparse
    4, -- cow
    cast(split_part(memory_volume, ',', 5) as uuid)
FROM snapshots
WHERE length(memory_volume) != 0 AND snapshot_type = 'ACTIVE' AND
    vm_id IN (SELECT vm_guid FROM vm_dynamic WHERE status = '13');

-- memory dump image
INSERT INTO images(
    image_guid,
    creation_date,
    size,
    it_guid,
    imagestatus,
    volume_type,
    volume_format,
    image_group_id
    )
SELECT
    cast(split_part(memory_volume, ',', 4) as uuid),
    'now',
    (mem_size_mb::bigint + 200 + (64 * num_of_monitors)) * 1024 * 1024,
    '00000000-0000-0000-0000-000000000000',
    1, -- ok
    0, -- unknown (will be updated by the next step)
    5, -- raw
    cast(split_part(memory_volume, ',', 3) as uuid)
FROM snapshots
JOIN vm_static ON vm_guid = vm_id
WHERE length(memory_volume) != 0 AND snapshot_type = 'ACTIVE' AND
    vm_id IN (SELECT vm_guid FROM vm_dynamic WHERE status = '13');

-- update volume type to pre-allocated for memory dumps on block devices
UPDATE images
SET volume_type = 1 -- pre-allocated
FROM snapshots
WHERE
   length(memory_volume) != 0 AND snapshot_type = 'ACTIVE' AND
   vm_id IN (SELECT vm_guid FROM vm_dynamic WHERE status = '13') AND
   image_guid = cast(split_part(memory_volume, ',', 4) as uuid) AND
   (SELECT storage_type from storage_domain_static where id = cast(split_part(memory_volume, ',', 1) as uuid)) IN (2, 3);

-- update volume type to sparse for all the other memory dumps
UPDATE images
SET volume_type = 2 -- sparse
FROM snapshots
WHERE
   volume_type = 0 AND length(memory_volume) != 0 AND snapshot_type = 'ACTIVE' AND
   image_guid = cast(split_part(memory_volume, ',', 4) as uuid) AND
   vm_id IN (SELECT vm_guid FROM vm_dynamic WHERE status = '13');

-- add to image <-> storage domain map
INSERT INTO image_storage_domain_map(
    image_id,
    storage_domain_id
    )
SELECT
    cast(split_part(memory_volume, ',', 4) as uuid),
    cast(split_part(memory_volume, ',', 1) as uuid)
FROM snapshots
WHERE length(memory_volume) != 0 AND snapshot_type = 'ACTIVE' AND
    vm_id IN (SELECT vm_guid FROM vm_dynamic WHERE status = '13');

INSERT INTO image_storage_domain_map(
    image_id,
    storage_domain_id
    )
SELECT
    cast(split_part(memory_volume, ',', 6) as uuid),
    cast(split_part(memory_volume, ',', 1) as uuid)
FROM snapshots
WHERE length(memory_volume) != 0 AND snapshot_type = 'ACTIVE' AND
    vm_id IN (SELECT vm_guid FROM vm_dynamic WHERE status = '13');

-- add base disks
INSERT INTO base_disks(
   disk_id,
   disk_interface,
   disk_alias,
   disk_description,
   shareable,
   boot
   )
SELECT
   cast(split_part(memory_volume, ',', 5) as uuid),
   'VirtIO',
   (SELECT vm_name FROM vm_static WHERE vm_guid = vm_id)||'_memory_metadata',
   'Hibernation meta-data',
   false,
   false
FROM snapshots
WHERE length(memory_volume) != 0 AND snapshot_type = 'ACTIVE' AND
   vm_id IN (SELECT vm_guid FROM vm_dynamic WHERE status = '13');

INSERT INTO base_disks(
   disk_id,
   disk_interface,
   disk_alias,
   disk_description,
   shareable,
   boot
   )
SELECT
   cast(split_part(memory_volume, ',', 3) as uuid),
   'VirtIO',
   (SELECT vm_name FROM vm_static WHERE vm_guid = vm_id)||'_memory_dump',
   'Hibernation memory-dump',
   false,
   false
FROM snapshots
WHERE length(memory_volume) != 0 AND snapshot_type = 'ACTIVE' AND
   vm_id IN (SELECT vm_guid FROM vm_dynamic WHERE status = '13');

-- update memory disks in active snapshots
UPDATE snapshots SET
   memory_dump_disk_id = cast(split_part(memory_volume, ',', 3) as uuid),
   memory_metadata_disk_id = cast(split_part(memory_volume, ',', 5) as uuid)
WHERE length(memory_volume) != 0 AND snapshot_type = 'ACTIVE' AND
   vm_id IN (SELECT vm_guid FROM vm_dynamic WHERE status = '13');


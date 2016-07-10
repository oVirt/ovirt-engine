-- remove memory that resides on storage domain that does not exist anymore
UPDATE snapshots
SET memory_volume = NULL
WHERE CAST(split_part(memory_volume, ',', 1) AS UUID) NOT IN (
        SELECT id
        FROM storage_domain_static
        );

-- add meta data images
INSERT INTO images(
    image_guid,
    creation_date,
    size,
    it_guid,
    imagestatus,
    volume_type,
    volume_format,
    image_group_id)
SELECT DISTINCT
    CAST(split_part(memory_volume, ',', 6) AS UUID),
    now(),
    10240,
    CAST('00000000-0000-0000-0000-000000000000' AS UUID),
    1, -- ok
    1, -- preallocated
    5, -- raw
    CAST(split_part(memory_volume, ',', 5) AS UUID)
FROM snapshots
WHERE length(memory_volume) != 0
    AND memory_dump_disk_id IS NULL;

CREATE FUNCTION _temp_getVirtualMemoryQuantity(text) RETURNS int AS $$
BEGIN
    RETURN (SELECT (xpath('//ovf:Envelope/Content/Section/Item[rasd:ResourceType/text() = 4]/rasd:VirtualQuantity/text()', XMLPARSE(CONTENT $1), ARRAY[ARRAY['ovf', 'http://schemas.dmtf.org/ovf/envelope/1/'], ARRAY['rasd', 'http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData']]))[1]::text::int);
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION _temp_getVirtualMonitorsQuantity(text) RETURNS int AS $$
BEGIN
    RETURN (SELECT (xpath('//ovf:Envelope/Content/Section/Item[rasd:ResourceType/text() = 20]/rasd:VirtualQuantity/text()', XMLPARSE(CONTENT $1), ARRAY[ARRAY['ovf', 'http://schemas.dmtf.org/ovf/envelope/1/'], ARRAY['rasd', 'http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData']]))[1]::text::int);
END;
$$ LANGUAGE plpgsql;

-- add memory dump images
INSERT INTO images(
    image_guid,
    creation_date,
    size,
    it_guid,
    imagestatus,
    volume_type,
    volume_format,
    image_group_id)
SELECT DISTINCT
    CAST(split_part(memory_volume, ',', 4) AS UUID),
    now(),
    (_temp_getVirtualMemoryQuantity(vm_configuration)::bigint + 200 + (64 * _temp_getVirtualMonitorsQuantity(vm_configuration))) * 1024 * 1024,
    CAST('00000000-0000-0000-0000-000000000000' AS UUID),
    1, -- ok
    0, -- unknown (will be updated by the next step)
    5, -- raw
    CAST(split_part(memory_volume, ',', 3) AS UUID)
FROM snapshots
WHERE length(memory_volume) != 0
    AND memory_dump_disk_id IS NULL;

DROP FUNCTION _temp_getVirtualMemoryQuantity(text);
DROP FUNCTION _temp_getVirtualMonitorsQuantity(text);

-- update volume type to preallocated for memory dumps on block devices
UPDATE images
SET volume_type = 1 -- preallocated
FROM snapshots
WHERE length(memory_volume) != 0
    AND memory_dump_disk_id IS NULL
    AND image_guid = CAST(split_part(memory_volume, ',', 4) AS UUID)
    AND (
        SELECT storage_type
        FROM storage_domain_static
        WHERE id = CAST(split_part(memory_volume, ',', 1) AS UUID)
        )
        IN (2, 3);

-- update volume type to sparse for all the other memory dumps
UPDATE images
SET volume_type = 2 -- sparse
FROM snapshots
WHERE volume_type = 0
    AND length(memory_volume) != 0
    AND memory_dump_disk_id IS NULL
    AND image_guid = CAST(split_part(memory_volume, ',', 4) AS UUID);

-- add image <-> storage domain mapping
INSERT INTO image_storage_domain_map(image_id,
                                     storage_domain_id)
SELECT DISTINCT
    CAST(split_part(memory_volume, ',', 4) AS UUID),
    CAST(split_part(memory_volume, ',', 1) AS UUID)
FROM snapshots
WHERE length(memory_volume) != 0
    AND memory_dump_disk_id IS NULL;

INSERT INTO image_storage_domain_map(image_id,
                                     storage_domain_id)
SELECT DISTINCT
    CAST(split_part(memory_volume, ',', 6) AS UUID),
    CAST(split_part(memory_volume, ',', 1) AS UUID)
FROM snapshots
WHERE length(memory_volume) != 0
    AND memory_dump_disk_id IS NULL;

-- add base disks
INSERT INTO base_disks(
    disk_id,
    disk_interface,
    disk_alias,
    disk_description,
    shareable,
    boot)
SELECT DISTINCT
   CAST(split_part(memory_volume, ',', 5) AS UUID),
   'VirtIO',
   'snapshot_metadata',
   'metadata for snapshot',
   false,
   false
FROM snapshots
WHERE length(memory_volume) != 0
    AND memory_dump_disk_id IS NULL;

INSERT INTO base_disks(
    disk_id,
    disk_interface,
    disk_alias,
    disk_description,
    shareable,
    boot)
SELECT DISTINCT
   CAST(split_part(memory_volume, ',', 3) AS UUID),
   'VirtIO',
   'snapshot_memory',
   'memory dump for snapshot',
   false,
   false
FROM snapshots
WHERE length(memory_volume) != 0
    AND memory_dump_disk_id IS NULL;

-- update memory disks in active snapshots
UPDATE snapshots
SET memory_dump_disk_id = CAST(split_part(memory_volume, ',', 3) AS UUID),
    memory_metadata_disk_id = CAST(split_part(memory_volume, ',', 5) AS UUID)
WHERE length(memory_volume) != 0
    AND memory_dump_disk_id IS NULL;


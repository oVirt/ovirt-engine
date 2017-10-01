UPDATE images
SET active = true
FROM snapshots sn
WHERE images.image_group_id IN (sn.memory_metadata_disk_id, sn.memory_dump_disk_id);

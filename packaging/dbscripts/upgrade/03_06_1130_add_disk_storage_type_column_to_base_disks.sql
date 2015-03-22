select fn_db_add_column('base_disks', 'disk_storage_type', 'SMALLINT DEFAULT NULL');

UPDATE base_disks SET disk_storage_type =
CASE
    WHEN EXISTS (
        SELECT 1
        FROM images
        WHERE images.image_group_id = base_disks.disk_id)
        THEN 0
        -- 0 is image
    WHEN EXISTS (
        SELECT 1
        FROM disk_lun_map
        WHERE disk_lun_map.disk_id = base_disks.disk_id)
        THEN 1
        -- 1 is lun
END;
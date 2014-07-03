DELETE FROM disk_image_dynamic
WHERE image_id NOT IN (SELECT image_guid FROM images);

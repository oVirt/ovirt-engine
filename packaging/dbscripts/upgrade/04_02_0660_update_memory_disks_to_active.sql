UPDATE images
SET active = true
FROM base_disks bd
WHERE images.image_group_id = bd.disk_id AND bd.disk_content_type IN (2, 3);

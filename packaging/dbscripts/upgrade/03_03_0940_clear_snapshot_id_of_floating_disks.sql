UPDATE images
SET    vm_snapshot_id = NULL
WHERE  image_group_id NOT IN (SELECT device_id FROM vm_device);
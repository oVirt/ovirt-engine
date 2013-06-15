INSERT INTO stateless_vm_image_map (vm_guid, image_guid, internal_drive_mapping)
(SELECT vm_guid, image_guid, internal_drive_mapping from image_vm_pool_map);

DROP table image_vm_pool_map;

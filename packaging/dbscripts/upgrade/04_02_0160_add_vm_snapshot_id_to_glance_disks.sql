UPDATE images
SET vm_snapshot_id = uuid_generate_v1()
WHERE images.image_group_id IN (SELECT images.image_group_id
                                FROM   images, vm_static, disk_vm_element
                                WHERE  images.image_group_id = disk_vm_element.disk_id AND
                                       vm_static.vm_guid = disk_vm_element.vm_id AND
                                       images.vm_snapshot_id is NULL AND
                                       vm_static.entity_type = 'TEMPLATE')
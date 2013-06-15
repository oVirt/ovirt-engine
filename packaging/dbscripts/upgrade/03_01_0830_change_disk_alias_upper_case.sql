UPDATE DISKS
SET disk_alias = vm_name || '_Disk' || disks.internal_drive_mapping
FROM vm_static, images, vm_device
WHERE vm_device.vm_id = vm_static.vm_guid
  AND vm_device.device_id = images.image_group_id
  AND disks.disk_id = images.image_group_id
  AND disks.disk_alias like '%_DISK%';

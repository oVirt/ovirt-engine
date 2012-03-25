-- Upgrade script for disks.
UPDATE disks
SET disk_alias = vm_name || '_DISK' || disks.internal_drive_mapping
FROM vm_static, images, vm_device
WHERE vm_device.vm_id = vm_static.vm_guid
  AND vm_device.device_id = images.image_group_id
  AND disks.disk_id = images.image_group_id
  AND disks.disk_alias IS NULL;

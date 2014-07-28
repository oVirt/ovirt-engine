-- Since we don't allow disks to be both bootable and shareable,
-- we need to set these disks "boot" value to false in base_disks,
-- and boot_order to 0 in vm_device.

UPDATE  vm_device
SET     boot_order = 0
WHERE   device_id IN (SELECT  disk_id
                      FROM    base_disks
                      WHERE   boot AND shareable);

UPDATE  base_disks
SET     boot = FALSE
WHERE   boot AND shareable;

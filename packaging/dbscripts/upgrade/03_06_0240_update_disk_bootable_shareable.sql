SELECT  03060240;

-- Following the decision to allow disks to be both bootable and shareable at
-- the same time,
-- (see discussion on https://bugzilla.redhat.com/1084103)
-- the original script below is no longer relevant.
--
-- It's included below for prosperity (commented out, of course), in case
-- anyone needs it for reference.
-- Note this was never merged to any release of oVirt, but it may have effected
-- users using the upstream master branch.
--
-- ORIGINAL SCRIPT:
--
-- Since we don't allow disks to be both bootable and shareable,
-- we need to set these disks "boot" value to false in base_disks,
-- and boot_order to 0 in vm_device.

-- UPDATE  vm_device
-- SET     boot_order = 0
-- WHERE   device_id IN (SELECT  disk_id
--                       FROM    base_disks
--                       WHERE   boot AND shareable);
--
-- UPDATE  base_disks
-- SET     boot = FALSE
-- WHERE   boot AND shareable;


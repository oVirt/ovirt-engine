-- Since SCSI device pass-through is not supported for read-only disks,
-- nullify 'sgio' property for such disks.
-- As discussed in BZ1118847, read-only/virtio-scsi/direct-lun disks
-- should use SCSI device emulation instead.

UPDATE base_disks
SET sgio = NULL
WHERE base_disks.disk_id IN (SELECT device_id FROM vm_device WHERE is_readonly = TRUE);

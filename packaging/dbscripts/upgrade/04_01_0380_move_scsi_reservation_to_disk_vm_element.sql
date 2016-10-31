SELECT fn_db_add_column('disk_vm_element', 'is_using_scsi_reservation', 'BOOLEAN NOT NULL DEFAULT FALSE');

UPDATE disk_vm_element dve
SET is_using_scsi_reservation = vd.is_using_scsi_reservation
FROM vm_device vd
WHERE dve.disk_id = vd.device_id AND dve.vm_id = vd.vm_id;

SELECT fn_db_drop_column('vm_device', 'is_using_scsi_reservation');

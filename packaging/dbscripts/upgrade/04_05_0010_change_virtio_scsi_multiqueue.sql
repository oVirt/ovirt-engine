
SELECT fn_db_add_column('vm_static', 'virtio_scsi_multi_queues', 'INTEGER NOT NULL DEFAULT 0');

-- value -1 indicates that the field value is set to automatic
UPDATE vm_static SET virtio_scsi_multi_queues=-1 WHERE virtio_scsi_multi_queues_enabled;

SELECT fn_db_drop_column('vm_static','virtio_scsi_multi_queues_enabled');
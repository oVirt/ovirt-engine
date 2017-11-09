-- Remove idx in order to create it as non-unique
select fn_db_drop_index('idx_unregistered_disks_storage_to_vms_unique');

-- Create the index again without the UNIQUE limitation
select fn_db_create_index('idx_unregistered_disks_storage_to_vms', 'unregistered_disks_to_vms', 'disk_id,storage_domain_id', '', false)

-- Adding storage_domain_id column so we can define a foreign key with unregistered_disks table.
SELECT fn_db_add_column('unregistered_disks_to_vms','storage_domain_id','uuid');

UPDATE unregistered_disks_to_vms udtv
SET storage_domain_id = unreg.storage_domain_id
FROM unregistered_disks unreg
WHERE unreg.disk_id = udtv.disk_id;

-- Update the primary key to contain also storage domain id,
-- since copied template disk will obtain the same disk_id and entity_id.
SELECT fn_db_drop_constraint('unregistered_disks_to_vms', 'pk_disk_id_unregistered');
SELECT fn_db_create_constraint('unregistered_disks_to_vms', 'pk_unregistered_disks_to_vms',
 'PRIMARY KEY(disk_id, entity_id, storage_domain_id)');


-- dropping foreign key to create it later with storage_domain_id
SELECT fn_db_drop_constraint('unregistered_disks_to_vms', 'fk_unregistered_disks_to_vms');

-- We can have copied template disks so there is no need for unique constraint in unregistered_disks.
SELECT fn_db_drop_constraint('unregistered_disks', 'unregistered_disks_disk_id_key');

-- Create unique index to include storage_domain_id and create a new foreign key which include it.
CREATE UNIQUE INDEX IDX_unregistered_disks_storage_to_vms_unique ON unregistered_disks_to_vms(disk_id, storage_domain_id);
SELECT fn_db_create_constraint('unregistered_disks_to_vms', 'fk_unregistered_disks_to_vms',
 'FOREIGN KEY (disk_id, storage_domain_id) REFERENCES unregistered_disks(disk_id, storage_domain_id) ON DELETE CASCADE');

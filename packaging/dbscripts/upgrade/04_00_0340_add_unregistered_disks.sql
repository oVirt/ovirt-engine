CREATE TABLE unregistered_disks
(
   disk_id UUID UNIQUE,
   disk_alias VARCHAR(255),
   disk_description VARCHAR(255),
   storage_domain_id UUID,
   creation_date DATE,
   last_modified DATE,
   volume_type INTEGER,
   volume_format INTEGER,
   actual_size bigint,
   size bigint,
   CONSTRAINT pk_disk_id_storage_domain_unregistered PRIMARY KEY(disk_id, storage_domain_id)
);

SELECT fn_db_create_constraint('unregistered_disks', 'fk_unregistered_disks_storage_domain', 'FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE');


CREATE TABLE unregistered_disks_to_vms
(
   disk_id UUID,
   entity_id UUID,
   entity_name VARCHAR(255),
   CONSTRAINT pk_disk_id_unregistered PRIMARY KEY(disk_id, entity_id)
);

SELECT fn_db_create_constraint('unregistered_disks_to_vms', 'fk_unregistered_disks_to_vms', 'FOREIGN KEY (disk_id) REFERENCES unregistered_disks(disk_id) ON DELETE CASCADE');
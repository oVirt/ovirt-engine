-- Clear all the data from host_device table
DELETE FROM host_device;

-- ----------------------------------------------------------------------
--  table host_nic_vfs_config
-- ----------------------------------------------------------------------

CREATE TABLE host_nic_vfs_config
(
  id UUID NOT NULL,
  nic_id UUID NOT NULL,
  is_all_networks_allowed BOOLEAN NOT NULL,
  _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
  _update_date TIMESTAMP WITH TIME ZONE default NULL
);


select fn_db_create_constraint('host_nic_vfs_config', 'vfs_config_id_pk', 'PRIMARY KEY(id)');
select fn_db_create_constraint('host_nic_vfs_config', 'vfs_config_nic_id_fk', 'FOREIGN KEY(nic_id) REFERENCES vds_interface(id) ON DELETE CASCADE');
select fn_db_create_constraint('host_nic_vfs_config', 'vfs_config_nic_id_unique', 'unique (nic_id)');

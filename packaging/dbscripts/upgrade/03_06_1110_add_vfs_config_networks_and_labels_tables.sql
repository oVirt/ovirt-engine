-- ----------------------------------------------------------------------
--  table vfs_config_networks
-- ----------------------------------------------------------------------

CREATE TABLE vfs_config_networks
(
  vfs_config_id UUID NOT NULL,
  network_id UUID NOT NULL,
  _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
  _update_date TIMESTAMP WITH TIME ZONE default NULL
);


select fn_db_create_constraint('vfs_config_networks', 'vfs_config_networks_pk', 'PRIMARY KEY(vfs_config_id, network_id)');
select fn_db_create_constraint('vfs_config_networks', 'vfs_config_networks_id_fk', 'FOREIGN KEY(vfs_config_id) REFERENCES host_nic_vfs_config(id) ON DELETE CASCADE');
select fn_db_create_constraint('vfs_config_networks', 'vfs_config_networks_network_fk', 'FOREIGN KEY(network_id) REFERENCES network(id) ON DELETE CASCADE');
select fn_db_create_index('IDX_vfs_config_networks_vfs_config_id', 'vfs_config_networks', 'vfs_config_id', '');


-- ----------------------------------------------------------------------
--  table vfs_config_labels
-- ----------------------------------------------------------------------

CREATE TABLE vfs_config_labels
(
  vfs_config_id UUID NOT NULL,
  label TEXT NOT NULL,
  _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
  _update_date TIMESTAMP WITH TIME ZONE default NULL
);


select fn_db_create_constraint('vfs_config_labels', 'vfs_config_labels_pk', 'PRIMARY KEY(vfs_config_id, label)');
select fn_db_create_constraint('vfs_config_labels', 'vfs_config_labels_id_fk', 'FOREIGN KEY(vfs_config_id) REFERENCES host_nic_vfs_config(id) ON DELETE CASCADE');
select fn_db_create_index('IDX_vfs_config_labels_vfs_config_id', 'vfs_config_labels', 'vfs_config_id', '');

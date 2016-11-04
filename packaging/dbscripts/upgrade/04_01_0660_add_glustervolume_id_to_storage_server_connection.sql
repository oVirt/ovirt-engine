SELECT fn_db_add_column('storage_server_connections', 'gluster_volume_id', 'UUID DEFAULT NULL');
SELECT fn_db_create_constraint('storage_server_connections', 'fk_storage_connection_to_glustervolume',
 'FOREIGN KEY (gluster_volume_id) REFERENCES gluster_volumes(id)');

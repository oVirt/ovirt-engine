select fn_db_add_column('gluster_volume_bricks', 'network_id', 'uuid NULL');

SELECT fn_db_create_constraint('gluster_volume_bricks', 'fk_gluster_volume_bricks_network_id', 'FOREIGN KEY (network_id) REFERENCES network(id)');

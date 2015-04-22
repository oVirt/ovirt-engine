SELECT  fn_db_drop_constraint('gluster_volume_bricks', 'fk_gluster_volume_bricks_network_id');
SELECT fn_db_create_constraint('gluster_volume_bricks', 'fk_gluster_volume_bricks_network_id', 'FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE RESTRICT');


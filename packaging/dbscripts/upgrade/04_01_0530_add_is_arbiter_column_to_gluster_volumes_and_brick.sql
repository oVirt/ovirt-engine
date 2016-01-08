SELECT fn_db_add_column('gluster_volume_bricks', 'is_arbiter', 'BOOLEAN DEFAULT FALSE');
SELECT fn_db_add_column('gluster_volumes', 'is_arbiter', 'BOOLEAN DEFAULT FALSE');
-- Add the "id" column and make it the primary key
SELECT fn_db_add_column('gluster_volume_bricks', 'id', 'UUID');
UPDATE gluster_volume_bricks set id = uuid_generate_v1();
SELECT fn_db_drop_constraint('gluster_volume_bricks','pk_gluster_volume_bricks');
SELECT fn_db_create_constraint('gluster_volume_bricks', 'pk_gluster_volume_bricks', 'PRIMARY KEY(id)');
SELECT fn_db_create_constraint('gluster_volume_bricks', 'IDX_gluster_volume_bricks_volume_server_brickdir', 'UNIQUE(volume_id, server_id, brick_dir)');

-- Add the "id" column and make it the primary key
SELECT fn_db_add_column('gluster_volume_options', 'id', 'UUID');
UPDATE gluster_volume_options set id = uuid_generate_v1();
SELECT fn_db_drop_constraint('gluster_volume_options','pk_gluster_volume_options');
SELECT fn_db_create_constraint('gluster_volume_options', 'pk_gluster_volume_options', 'PRIMARY KEY(id)');
SELECT fn_db_create_constraint('gluster_volume_options', 'IDX_gluster_volume_options_volume_id_option_key', 'UNIQUE(volume_id, option_key)');


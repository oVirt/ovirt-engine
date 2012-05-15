-- Add the "id" column and make it the primary key
SELECT fn_db_add_column('gluster_volume_bricks', 'id', 'UUID');
UPDATE gluster_volume_bricks set id = uuid_generate_v1();
ALTER TABLE gluster_volume_bricks DROP CONSTRAINT pk_gluster_volume_bricks;
ALTER TABLE gluster_volume_bricks ADD CONSTRAINT pk_gluster_volume_bricks PRIMARY KEY(id);
ALTER TABLE gluster_volume_bricks ADD CONSTRAINT IDX_gluster_volume_bricks_volume_server_brickdir UNIQUE(volume_id, server_id, brick_dir);

-- Add the "id" column and make it the primary key
SELECT fn_db_add_column('gluster_volume_options', 'id', 'UUID');
UPDATE gluster_volume_options set id = uuid_generate_v1();
ALTER TABLE gluster_volume_options DROP CONSTRAINT pk_gluster_volume_options;
ALTER TABLE gluster_volume_options ADD CONSTRAINT pk_gluster_volume_options PRIMARY KEY(id);
ALTER TABLE gluster_volume_options ADD CONSTRAINT IDX_gluster_volume_options_volume_id_option_key UNIQUE(volume_id, option_key);


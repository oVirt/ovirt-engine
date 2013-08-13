-- adding task_id column to gluster volumes used to store gluster task id returned
SELECT fn_db_add_column('gluster_volumes', 'task_id', 'uuid');

-- adding task_id column to gluster volume bricks used to store gluster task id returned
SELECT fn_db_add_column('gluster_volume_bricks', 'task_id', 'uuid');

CREATE INDEX IDX_step_external_id ON step(external_id);

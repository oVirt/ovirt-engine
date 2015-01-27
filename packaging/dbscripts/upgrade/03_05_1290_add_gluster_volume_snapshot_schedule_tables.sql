-- Add gluster_volume_snapshot_schedules table
CREATE TABLE gluster_volume_snapshot_schedules
(
    volume_id UUID NOT NULL,
    job_id VARCHAR(256) NOT NULL,
    snapshot_name_prefix VARCHAR(128),
    snapshot_description VARCHAR(1024),
    recurrence VARCHAR(128) NOT NULL,
    time_zone VARCHAR(128),
    interval INTEGER,
    start_date TIMESTAMP WITH TIME ZONE,
    execution_time TIME,
    days VARCHAR(256),
    end_by TIMESTAMP WITH TIME ZONE,
    _create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    _update_date TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_volume_id PRIMARY KEY(volume_id)
) WITH OIDS;

SELECT fn_db_create_constraint('gluster_volume_snapshot_schedules', 'fk_gluster_volume_snapshot_schedules_volume_id', 'FOREIGN KEY (volume_id) REFERENCES gluster_volumes(id) ON DELETE CASCADE');


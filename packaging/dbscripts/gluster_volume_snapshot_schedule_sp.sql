

/* ----------------------------------------------------------------
 Stored procedures for database operations on Gluster Volume Snapshot
 related tables:
      - gluster_volume_snapshot_schedules
----------------------------------------------------------------*/
CREATE OR REPLACE FUNCTION InsertGlusterVolumeSnapshotSchedule (
    v_volume_id UUID,
    v_job_id VARCHAR(256),
    v_snapshot_name_prefix VARCHAR(128),
    v_snapshot_description VARCHAR(1024),
    v_recurrence VARCHAR(128),
    v_time_zone VARCHAR(128),
    v_interval INT,
    v_start_date TIMESTAMP WITH TIME ZONE,
    v_execution_time TIME,
    v_days VARCHAR(256),
    v_end_by TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_volume_snapshot_schedules (
        volume_id,
        job_id,
        snapshot_name_prefix,
        snapshot_description,
        recurrence,
        time_zone,
        interval,
        start_date,
        execution_time,
        days,
        end_by
        )
    VALUES (
        v_volume_id,
        v_job_id,
        v_snapshot_name_prefix,
        v_snapshot_description,
        v_recurrence,
        v_time_zone,
        v_interval,
        v_start_date,
        v_execution_time,
        v_days,
        v_end_by
        );

    UPDATE gluster_volumes
    SET snapshot_scheduled = true
    WHERE id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeSnapshotScheduleByVolumeId (v_volume_id UUID)
RETURNS SETOF gluster_volume_snapshot_schedules_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_snapshot_schedules_view
    WHERE volume_id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeSnapshotScheduleByVolumeId (v_volume_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volume_snapshot_schedules
    WHERE volume_id = v_volume_id;

    UPDATE gluster_volumes
    SET snapshot_scheduled = false
    WHERE id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeSnapshotScheduleByVolumeId (
    v_volume_id UUID,
    v_job_id VARCHAR(256),
    v_snapshot_name_prefix VARCHAR(128),
    v_snapshot_description VARCHAR(1024),
    v_recurrence VARCHAR(128),
    v_time_zone VARCHAR(128),
    v_interval INT,
    v_start_date TIMESTAMP WITH TIME ZONE,
    v_execution_time TIME,
    v_days VARCHAR(256),
    v_end_by TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_snapshot_schedules
    SET job_id = v_job_id,
        snapshot_name_prefix = v_snapshot_name_prefix,
        snapshot_description = v_snapshot_description,
        recurrence = v_recurrence,
        time_zone = v_time_zone,
        interval = v_interval,
        start_date = v_start_date,
        execution_time = v_execution_time,
        days = v_days,
        end_by = v_end_by,
        _update_date = LOCALTIMESTAMP
    WHERE volume_id = v_volume_id;

    UPDATE gluster_volumes
    SET snapshot_scheduled = true
    WHERE id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



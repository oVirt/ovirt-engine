

/* ----------------------------------------------------------------
 Stored procedures for database operations on Gluster Volume Snapshot
 related tables:
      - gluster_volume_snapshots
      - gluster_volume_snapshot_config
----------------------------------------------------------------*/
CREATE OR REPLACE FUNCTION InsertGlusterVolumeSnapshot (
    v_snapshot_id UUID,
    v_snapshot_name VARCHAR(1000),
    v_volume_id UUID,
    v_description VARCHAR(1024),
    v_status VARCHAR(32),
    v__create_date TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_volume_snapshots (
        snapshot_id,
        snapshot_name,
        volume_id,
        description,
        status,
        _create_date
        )
    VALUES (
        v_snapshot_id,
        v_snapshot_name,
        v_volume_id,
        v_description,
        v_status,
        v__create_date
        );

    PERFORM UpdateSnapshotCountInc(v_volume_id, 1);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeSnapshotById (v_snapshot_id UUID)
RETURNS SETOF gluster_volume_snapshots_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_snapshots_view
    WHERE snapshot_id = v_snapshot_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeSnapshotsByVolumeId (v_volume_id UUID)
RETURNS SETOF gluster_volume_snapshots_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_snapshots_view
    WHERE volume_id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeSnapshotsByClusterId (v_cluster_id UUID)
RETURNS SETOF gluster_volume_snapshots_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_snapshots_view
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeSnapshotByName (
    v_volume_id UUID,
    v_snapshot_name VARCHAR(1000)
    )
RETURNS SETOF gluster_volume_snapshots_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_snapshots_view
    WHERE volume_id = v_volume_id
        AND snapshot_name = v_snapshot_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeSnapshotByGuid (v_snapshot_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE ref_volume_id UUID;

BEGIN
    SELECT volume_id
    INTO ref_volume_id
    FROM gluster_volume_snapshots
    WHERE snapshot_id = v_snapshot_id;

    DELETE
    FROM gluster_volume_snapshots
    WHERE snapshot_id = v_snapshot_id;

    PERFORM UpdateSnapshotCountDec(ref_volume_id, 1);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeSnapshotsByVolumeId (v_volume_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volume_snapshots
    WHERE volume_id = v_volume_id;

    UPDATE gluster_volumes
    SET snapshot_count = 0
    WHERE id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeSnapshotByName (
    v_volume_id UUID,
    v_snapshot_name VARCHAR(1000)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volume_snapshots
    WHERE volume_id = v_volume_id
        AND snapshot_name = v_snapshot_name;

    PERFORM UpdateSnapshotCountDec(v_volume_id, 1);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumesSnapshotByIds (v_snapshot_ids VARCHAR(5000))
RETURNS VOID AS $PROCEDURE$
DECLARE v_volume_id UUID;

v_snapshot_count INT;

v_cur CURSOR
FOR

SELECT volume_id,
    count(volume_id)
FROM gluster_volume_snapshots
WHERE snapshot_id IN (
        SELECT *
        FROM fnSplitterUuid(v_snapshot_ids)
        )
GROUP BY volume_id;

BEGIN
    OPEN v_cur;

    LOOP

    FETCH v_cur
    INTO v_volume_id,
        v_snapshot_count;

    EXIT WHEN NOT FOUND;

    PERFORM UpdateSnapshotCountDec(v_volume_id, v_snapshot_count);
END LOOP;

CLOSE v_cur;

DELETE
FROM gluster_volume_snapshots
WHERE snapshot_id IN (
        SELECT *
        FROM fnSplitterUuid(v_snapshot_ids)
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeSnapshotStatus (
    v_snapshot_id UUID,
    v_status VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_snapshots
    SET status = v_status,
        _update_date = LOCALTIMESTAMP
    WHERE snapshot_id = v_snapshot_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeSnapshotStatusByName (
    v_volume_id UUID,
    v_snapshot_name VARCHAR(1000),
    v_status VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_snapshots
    SET status = v_status,
        _update_date = LOCALTIMESTAMP
    WHERE volume_id = v_volume_id
        AND snapshot_name = v_snapshot_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertGlusterVolumeSnapshotConfig (
    v_cluster_id UUID,
    v_volume_id UUID,
    v_param_name VARCHAR(128),
    v_param_value VARCHAR(128)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_volume_snapshot_config (
        cluster_id,
        volume_id,
        param_name,
        param_value
        )
    VALUES (
        v_cluster_id,
        v_volume_id,
        v_param_name,
        v_param_value
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeSnapshotConfigByClusterId (v_cluster_id UUID)
RETURNS SETOF gluster_volume_snapshot_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_snapshot_config
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeSnapshotConfigByVolumeId (
    v_cluster_id UUID,
    v_volume_id UUID
    )
RETURNS SETOF gluster_volume_snapshot_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_snapshot_config
    WHERE cluster_id = v_cluster_id
        AND volume_id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeSnapshotConfigByClusterIdAndName (
    v_cluster_id UUID,
    v_param_name VARCHAR(128)
    )
RETURNS SETOF gluster_volume_snapshot_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_snapshot_config
    WHERE cluster_id = v_cluster_id
        AND volume_id IS NULL
        AND param_name = v_param_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeSnapshotConfigByVolumeIdAndName (
    v_cluster_id UUID,
    v_volume_id UUID,
    v_param_name VARCHAR(128)
    )
RETURNS SETOF gluster_volume_snapshot_config STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_snapshot_config
    WHERE cluster_id = v_cluster_id
        AND volume_id = v_volume_id
        AND param_name = v_param_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateConfigByClusterIdAndName (
    v_cluster_id UUID,
    v_param_name VARCHAR(128),
    v_param_value VARCHAR(128)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_snapshot_config
    SET param_value = v_param_value,
        _update_date = LOCALTIMESTAMP
    WHERE cluster_id = v_cluster_id
        AND volume_id IS NULL
        AND param_name = v_param_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateConfigByVolumeIdIdAndName (
    v_cluster_id UUID,
    v_volume_id UUID,
    v_param_name VARCHAR(128),
    v_param_value VARCHAR(128)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_snapshot_config
    SET param_value = v_param_value,
        _update_date = LOCALTIMESTAMP
    WHERE cluster_id = v_cluster_id
        AND volume_id = v_volume_id
        AND param_name = v_param_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateSnapshotCountInc (
    v_volume_id UUID,
    v_num INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volumes
    SET snapshot_count = snapshot_count + v_num
    WHERE id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateSnapshotCountDec (
    v_volume_id UUID,
    v_num INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volumes
    SET snapshot_count = snapshot_count - v_num
    WHERE id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



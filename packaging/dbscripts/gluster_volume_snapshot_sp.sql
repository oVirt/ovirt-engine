/* ----------------------------------------------------------------
 Stored procedures for database operations on Gluster Volume Snapshot
 related tables:
      - gluster_volume_snapshots
      - gluster_volume_snapshot_config
----------------------------------------------------------------*/

Create or replace FUNCTION InsertGlusterVolumeSnapshot(v_snapshot_id UUID,
                                                v_snapshot_name VARCHAR(1000),
                                                v_volume_id UUID,
                                                v_description VARCHAR(1024),
                                                v_status VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    INSERT INTO gluster_volume_snapshots (snapshot_id, snapshot_name, volume_id,
        description, status)
    VALUES (v_snapshot_id,  v_snapshot_name, v_volume_id,
        v_description, v_status);

    PERFORM UpdateSnapshotCountInc(v_volume_id, 1);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterVolumeSnapshotById(v_snapshot_id UUID)
    RETURNS SETOF gluster_volume_snapshots_view STABLE
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_volume_snapshots_view
    WHERE snapshot_id = v_snapshot_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterVolumeSnapshotsByVolumeId(v_volume_id UUID)
RETURNS SETOF gluster_volume_snapshots_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_volume_snapshots_view
    WHERE volume_id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterVolumeSnapshotsByClusterId(v_cluster_id UUID)
RETURNS SETOF gluster_volume_snapshots_view STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_volume_snapshots_view
    WHERE cluster_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterVolumeSnapshotByName(v_volume_id UUID,
                                            v_snapshot_name VARCHAR(1000))
RETURNS SETOF gluster_volume_snapshots_view STABLE
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM gluster_volume_snapshots_view
    WHERE volume_id = v_volume_id and snapshot_name = v_snapshot_name;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterVolumeSnapshotByGuid(v_snapshot_id UUID)
    RETURNS VOID
    AS $procedure$
DECLARE
ref_volume_id UUID;
BEGIN
    SELECT volume_id INTO ref_volume_id FROM gluster_volume_snapshots WHERE snapshot_id = v_snapshot_id;

    DELETE FROM gluster_volume_snapshots
    WHERE snapshot_id = v_snapshot_id;

    PERFORM UpdateSnapshotCountDec(ref_volume_id, 1);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterVolumeSnapshotsByVolumeId(v_volume_id UUID)
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volume_snapshots
    WHERE volume_id = v_volume_id;

    UPDATE gluster_volumes
    SET snapshot_count = 0
    WHERE id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterVolumeSnapshotByName(v_volume_id UUID,
                                                    v_snapshot_name VARCHAR(1000))
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volume_snapshots
    WHERE volume_id = v_volume_id
    AND   snapshot_name = v_snapshot_name;

    PERFORM UpdateSnapshotCountDec(v_volume_id, 1);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterVolumesSnapshotByIds(v_snapshot_ids VARCHAR(5000))
    RETURNS VOID
    AS $procedure$
DECLARE
v_volume_id UUID;
v_snapshot_count integer;
v_cur CURSOR FOR SELECT volume_id, count(volume_id) FROM gluster_volume_snapshots
WHERE snapshot_id IN (SELECT * FROM fnSplitterUuid(v_snapshot_ids)) GROUP BY volume_id;
BEGIN
    OPEN v_cur;
    LOOP
        FETCH v_cur INTO v_volume_id, v_snapshot_count;
        EXIT WHEN NOT FOUND;
        PERFORM UpdateSnapshotCountDec(v_volume_id, v_snapshot_count);
    END LOOP;
    CLOSE v_cur;

    DELETE FROM gluster_volume_snapshots
    WHERE snapshot_id in (select * from fnSplitterUuid(v_snapshot_ids));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterVolumeSnapshotStatus(v_snapshot_id UUID,
                                                        v_status VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE  gluster_volume_snapshots
    SET     status = v_status,
            _update_date = LOCALTIMESTAMP
    WHERE   snapshot_id = v_snapshot_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterVolumeSnapshotStatusByName(v_volume_id UUID,
                                                        v_snapshot_name VARCHAR(1000),
                                                        v_status VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE  gluster_volume_snapshots
    SET     status = v_status,
            _update_date = LOCALTIMESTAMP
    WHERE   volume_id = v_volume_id
    AND     snapshot_name = v_snapshot_name;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION InsertGlusterVolumeSnapshotConfig(v_cluster_id UUID,
                                                v_volume_id UUID,
                                                v_param_name VARCHAR(128),
                                                v_param_value VARCHAR(128))
    RETURNS VOID
    AS $procedure$
BEGIN
    INSERT INTO gluster_volume_snapshot_config (cluster_id, volume_id, param_name, param_value)
    VALUES (v_cluster_id, v_volume_id, v_param_name, v_param_value);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterVolumeSnapshotConfigByClusterId(v_cluster_id UUID)
    RETURNS SETOF gluster_volume_snapshot_config STABLE
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_volume_snapshot_config
    WHERE cluster_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterVolumeSnapshotConfigByVolumeId(v_cluster_id UUID, v_volume_id UUID)
RETURNS SETOF gluster_volume_snapshot_config STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_volume_snapshot_config
    WHERE cluster_id = v_cluster_id and volume_id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterVolumeSnapshotConfigByClusterIdAndName(v_cluster_id UUID,
                                            v_param_name VARCHAR(128))
RETURNS SETOF gluster_volume_snapshot_config STABLE
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM gluster_volume_snapshot_config
    WHERE cluster_id = v_cluster_id and volume_id IS NULL and param_name = v_param_name;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterVolumeSnapshotConfigByVolumeIdAndName(v_cluster_id UUID,
                                            v_volume_id UUID,
                                            v_param_name VARCHAR(128))
RETURNS SETOF gluster_volume_snapshot_config STABLE
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM gluster_volume_snapshot_config
    WHERE cluster_id = v_cluster_id and volume_id = v_volume_id and param_name = v_param_name;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateConfigByClusterIdAndName(v_cluster_id UUID,
                                            v_param_name VARCHAR(128),
                                            v_param_value VARCHAR(128))
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE  gluster_volume_snapshot_config
    SET     param_value = v_param_value,
            _update_date = LOCALTIMESTAMP
    WHERE   cluster_id = v_cluster_id
    AND     volume_id IS NULL
    AND     param_name = v_param_name;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateConfigByVolumeIdIdAndName(v_cluster_id UUID,
                                            v_volume_id UUID,
                                            v_param_name VARCHAR(128),
                                            v_param_value VARCHAR(128))
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE  gluster_volume_snapshot_config
    SET     param_value = v_param_value,
            _update_date = LOCALTIMESTAMP
    WHERE   cluster_id = v_cluster_id
    AND     volume_id = v_volume_id
    AND     param_name = v_param_name;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateSnapshotCountInc(v_volume_id UUID, v_num int)
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE gluster_volumes
    SET snapshot_count = snapshot_count + v_num
    WHERE id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateSnapshotCountDec(v_volume_id UUID, v_num int)
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE gluster_volumes
    SET snapshot_count = snapshot_count - v_num
    WHERE id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;

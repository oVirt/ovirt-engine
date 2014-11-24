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
BEGIN
    DELETE FROM gluster_volume_snapshots
    WHERE snapshot_id = v_snapshot_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterVolumeSnapshotsByVolumeId(v_volume_id UUID)
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volume_snapshots
    WHERE volume_id = v_volume_id;
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
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterVolumesSnapshotByIds(v_snapshot_ids VARCHAR(5000))
    RETURNS VOID
    AS $procedure$
BEGIN
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


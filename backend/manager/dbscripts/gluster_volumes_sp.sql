/* ----------------------------------------------------------------
 Stored procedures for database operations on Gluster Volume
 related tables:
      - gluster_volumes
      - gluster_volume_bricks
      - gluster_volume_options
      - gluster_volume_access_protocols
      - gluster_volume_transport_types
----------------------------------------------------------------*/

Create or replace FUNCTION InsertGlusterVolume(v_id UUID, v_cluster_id UUID,
                                                v_vol_name VARCHAR(1000),
                                                v_vol_type VARCHAR(32),
                                                v_status VARCHAR(32),
                                                v_replica_count INTEGER,
                                                v_stripe_count INTEGER)
    RETURNS VOID
    AS $procedure$
BEGIN
    INSERT INTO gluster_volumes (id, cluster_id, vol_name, vol_type,
        status, replica_count, stripe_count)
    VALUES (v_id,  v_cluster_id, v_vol_name, v_vol_type,
        v_status, v_replica_count,  v_stripe_count);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertGlusterVolumeBrick(v_id UUID, v_volume_id UUID,
                                                    v_server_id UUID,
                                                    v_brick_dir VARCHAR(4096),
                                                    v_brick_order INTEGER,
                                                    v_status VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    INSERT INTO gluster_volume_bricks (id, volume_id, server_id, brick_dir, brick_order, status)
    VALUES (v_id, v_volume_id, v_server_id, v_brick_dir, v_brick_order, v_status);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertGlusterVolumeOption(v_id UUID, v_volume_id UUID,
                                                    v_option_key VARCHAR(8192),
                                                    v_option_val VARCHAR(8192))
    RETURNS VOID
    AS $procedure$
BEGIN
    INSERT INTO gluster_volume_options (id, volume_id, option_key, option_val)
    VALUES (v_id, v_volume_id, v_option_key, v_option_val);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertGlusterVolumeAccessProtocol(v_volume_id UUID,
                                                    v_access_protocol VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    INSERT INTO gluster_volume_access_protocols (volume_id, access_protocol)
    VALUES (v_volume_id, v_access_protocol);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertGlusterVolumeTransportType(v_volume_id UUID,
                                                    v_transport_type VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    INSERT INTO gluster_volume_transport_types (volume_id, transport_type)
    VALUES (v_volume_id, v_transport_type);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterVolumesByClusterGuid(v_cluster_id UUID)
    RETURNS SETOF gluster_volumes_view
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_volumes_view
    WHERE cluster_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterVolumesByOption(v_cluster_id UUID,
                                                       v_status VARCHAR(32),
                                                       v_option_key VARCHAR(8192),
                                                       v_option_val VARCHAR(8192))
RETURNS SETOF gluster_volumes_view
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_volumes_view
    WHERE cluster_id = v_cluster_id AND status = v_status
    AND id IN (SELECT volume_id FROM gluster_volume_options
    WHERE option_key=v_option_key AND option_val=v_option_val);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterVolumesByStatusTypesAndOption(v_cluster_id UUID,
                                                                  v_status VARCHAR(32),
                                                                  v_vol_types text,
                                                                  v_option_key VARCHAR(8192),
                                                                  v_option_val VARCHAR(8192))
RETURNS SETOF gluster_volumes_view
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_volumes_view
    WHERE cluster_id = v_cluster_id AND status = v_status
    AND vol_type IN (SELECT ID FROM fnSplitter(v_vol_types))
    AND id IN (SELECT volume_id FROM gluster_volume_options
    WHERE option_key=v_option_key AND option_val=v_option_val);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterVolumesByStatusAndTypes(v_cluster_id UUID,
                                                         v_status VARCHAR(32),
                                                         v_vol_types text)
RETURNS SETOF gluster_volumes_view
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_volumes_view
    WHERE cluster_id = v_cluster_id AND status = v_status
    AND vol_type IN (SELECT ID FROM fnSplitter(v_vol_types));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterVolumeById(v_volume_id UUID)
    RETURNS SETOF gluster_volumes_view
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM gluster_volumes_view
    WHERE id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterVolumeByName(v_cluster_id UUID,
                                            v_vol_name VARCHAR(1000))
RETURNS SETOF gluster_volumes_view
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM gluster_volumes_view
    WHERE cluster_id = v_cluster_id and vol_name = v_vol_name;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterBrickById(v_id UUID)
    RETURNS SETOF gluster_volume_bricks
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM gluster_volume_bricks
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetBricksByGlusterVolumeGuid(v_volume_id UUID)
    RETURNS SETOF gluster_volume_bricks
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_volume_bricks
    WHERE volume_id = v_volume_id
    ORDER BY brick_order;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterVolumeBricksByServerGuid(v_server_id UUID)
RETURNS SETOF gluster_volume_bricks
AS $procedure$
BEGIN
RETURN QUERY SELECT *
FROM  gluster_volume_bricks
WHERE server_id = v_server_id
ORDER BY brick_order;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetGlusterOptionById(v_id UUID)
    RETURNS SETOF gluster_volume_options
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM gluster_volume_options
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetOptionsByGlusterVolumeGuid(v_volume_id UUID)
    RETURNS SETOF gluster_volume_options
    AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_volume_options
    WHERE volume_id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAccessProtocolsByGlusterVolumeGuid(v_volume_id UUID)
    RETURNS SETOF gluster_volume_access_protocols
       AS $procedure$
BEGIN
       RETURN QUERY SELECT *
       FROM  gluster_volume_access_protocols
       WHERE volume_id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetTransportTypesByGlusterVolumeGuid(v_volume_id UUID)
    RETURNS SETOF gluster_volume_transport_types
       AS $procedure$
BEGIN
       RETURN QUERY SELECT *
       FROM  gluster_volume_transport_types
       WHERE volume_id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterVolumeByGuid(v_volume_id UUID)
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volumes
    WHERE id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterVolumesByGuids(v_volume_ids VARCHAR(5000))
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volumes
    WHERE id in (select * from fnSplitterUuid(v_volume_ids));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterVolumeByName(v_cluster_id UUID,
                                                    v_vol_name VARCHAR(1000))
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volumes
    WHERE cluster_id = v_cluster_id
    AND   vol_name = v_vol_name;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteGlusterVolumesByClusterId(v_cluster_id UUID)
    RETURNS VOID
    AS $procedure$
BEGIN
DELETE FROM gluster_volumes
WHERE cluster_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteGlusterVolumeBrick(v_id UUID)
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volume_bricks
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteGlusterVolumeBricks(v_ids VARCHAR(5000))
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volume_bricks
    WHERE id in (select * from fnSplitterUuid(v_ids));
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteGlusterVolumeOption(v_id UUID)
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volume_options
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteGlusterVolumeOptions(v_ids VARCHAR(5000))
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volume_options
    WHERE id in (select * from fnSplitterUuid(v_ids));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterVolumeAccessProtocol(v_volume_id UUID,
                                                    v_access_protocol VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volume_access_protocols
    WHERE volume_id = v_volume_id
    AND   access_protocol = v_access_protocol;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterVolumeTransportType(v_volume_id UUID,
                                                    v_transport_type VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    DELETE FROM gluster_volume_transport_types
    WHERE volume_id = v_volume_id
    AND   transport_type = v_transport_type;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterVolume(v_id UUID,
                                                v_cluster_id UUID,
                                                v_vol_name VARCHAR(1000),
                                                v_vol_type VARCHAR(32),
                                                v_status VARCHAR(32),
                                                v_replica_count INTEGER,
                                                v_stripe_count INTEGER)
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE gluster_volumes
    SET cluster_id = v_cluster_id,
        vol_name = v_vol_name,
        vol_type = v_vol_type,
        status = v_status,
        replica_count = v_replica_count,
        stripe_count = v_stripe_count,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterVolumeBrick(v_id UUID,
                                                    v_new_id UUID,
                                                    v_new_server_id UUID,
                                                    v_new_brick_dir VARCHAR(4096),
                                                    v_new_status VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE gluster_volume_bricks
    SET    id = v_new_id,
           server_id = v_new_server_id,
           brick_dir = v_new_brick_dir,
           status = v_new_status,
           _update_date = LOCALTIMESTAMP
    WHERE  id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterVolumeBrickStatus(v_id UUID,
                                                        v_status VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE  gluster_volume_bricks
    SET     status = v_status,
            _update_date = LOCALTIMESTAMP
    WHERE   id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterVolumeBrickOrder(v_id UUID, v_brick_order INTEGER)
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE  gluster_volume_bricks
    SET     brick_order = v_brick_order,
            _update_date = LOCALTIMESTAMP
    WHERE   id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterVolumeStatus(v_volume_id UUID,
                                                    v_status VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE gluster_volumes
    SET
        status = v_status,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterVolumeStatusByName(v_cluster_id UUID,
                                                    v_vol_name VARCHAR(1000),
                                                    v_status VARCHAR(32))
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE gluster_volumes
    SET
        status = v_status,
        _update_date = LOCALTIMESTAMP
    WHERE cluster_id = v_cluster_id
    AND   vol_name = v_vol_name;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterVolumeOption(v_id UUID, v_option_val VARCHAR(8192))
    RETURNS VOID
    AS $procedure$
BEGIN
    UPDATE gluster_volume_options
    SET option_val = v_option_val
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateReplicaCount(v_volume_id UUID,
        v_replica_count INTEGER)
RETURNS VOID
AS $procedure$
BEGIN
UPDATE gluster_volumes
SET
replica_count = v_replica_count,
_update_date = LOCALTIMESTAMP
WHERE id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateStripeCount(v_volume_id UUID,
        v_stripe_count INTEGER)
RETURNS VOID
AS $procedure$
BEGIN
UPDATE gluster_volumes
SET
stripe_count = v_stripe_count,
_update_date = LOCALTIMESTAMP
WHERE id = v_volume_id;
END; $procedure$
LANGUAGE plpgsql;

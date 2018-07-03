

/* ----------------------------------------------------------------
 Stored procedures for database operations on Gluster Volume
 related tables:
      - gluster_volumes
      - gluster_volume_bricks
      - gluster_volume_options
      - gluster_volume_access_protocols
      - gluster_volume_transport_types
----------------------------------------------------------------*/
CREATE OR REPLACE FUNCTION InsertGlusterVolume (
    v_id UUID,
    v_cluster_id UUID,
    v_vol_name VARCHAR(1000),
    v_vol_type VARCHAR(32),
    v_status VARCHAR(32),
    v_replica_count INT,
    v_stripe_count INT,
    v_disperse_count INT,
    v_redundancy_count INT,
    v_is_arbiter boolean
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_volumes (
        id,
        cluster_id,
        vol_name,
        vol_type,
        status,
        replica_count,
        stripe_count,
        disperse_count,
        redundancy_count,
        is_arbiter
        )
    VALUES (
        v_id,
        v_cluster_id,
        v_vol_name,
        v_vol_type,
        v_status,
        v_replica_count,
        v_stripe_count,
        v_disperse_count,
        v_redundancy_count,
        v_is_arbiter
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertGlusterVolumeDetails (
    v_volume_id UUID,
    v_total_space BIGINT,
    v_used_space BIGINT,
    v_free_space BIGINT,
    v_confirmed_free_space BIGINT,
    v_vdo_savings INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_volume_details (
        volume_id,
        total_space,
        used_space,
        free_space,
        confirmed_free_space,
        vdo_savings,
        _update_date
        )
    VALUES (
        v_volume_id,
        v_total_space,
        v_used_space,
        v_free_space,
        v_confirmed_free_space,
        v_vdo_savings,
        LOCALTIMESTAMP
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertGlusterVolumeBrick (
    v_id UUID,
    v_volume_id UUID,
    v_server_id UUID,
    v_brick_dir VARCHAR(4096),
    v_brick_order INT,
    v_status VARCHAR(32),
    v_network_id UUID,
    v_is_arbiter boolean
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_volume_bricks (
        id,
        volume_id,
        server_id,
        brick_dir,
        brick_order,
        status,
        network_id,
        is_arbiter
        )
    VALUES (
        v_id,
        v_volume_id,
        v_server_id,
        v_brick_dir,
        v_brick_order,
        v_status,
        v_network_id,
        v_is_arbiter
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertGlusterVolumeBrickDetails (
    v_brick_id UUID,
    v_total_space BIGINT,
    v_used_space BIGINT,
    v_free_space BIGINT,
    v_confirmed_free_space BIGINT,
    v_confirmed_total_space BIGINT,
    v_vdo_savings INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_volume_brick_details (
        brick_id,
        total_space,
        used_space,
        free_space,
        confirmed_free_space,
        confirmed_total_space,
        vdo_savings,
        _update_date
        )
    VALUES (
        v_brick_id,
        v_total_space,
        v_used_space,
        v_free_space,
        v_confirmed_free_space,
        v_confirmed_total_space,
        v_vdo_savings,
        LOCALTIMESTAMP
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertGlusterVolumeOption (
    v_id UUID,
    v_volume_id UUID,
    v_option_key VARCHAR(8192),
    v_option_val VARCHAR(8192)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_volume_options (
        id,
        volume_id,
        option_key,
        option_val
        )
    VALUES (
        v_id,
        v_volume_id,
        v_option_key,
        v_option_val
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertGlusterVolumeAccessProtocol (
    v_volume_id UUID,
    v_access_protocol VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_volume_access_protocols (
        volume_id,
        access_protocol
        )
    VALUES (
        v_volume_id,
        v_access_protocol
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertGlusterVolumeTransportType (
    v_volume_id UUID,
    v_transport_type VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_volume_transport_types (
        volume_id,
        transport_type
        )
    VALUES (
        v_volume_id,
        v_transport_type
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumesByClusterGuid (v_cluster_id UUID)
RETURNS SETOF gluster_volumes_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volumes_view
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumesSupportedAsStorageDomain ()
RETURNS SETOF gluster_volumes_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM gluster_volumes_view
    WHERE vol_type IN ('REPLICATE', 'DISTRIBUTE', 'DISTRIBUTED_REPLICATE')
    AND replica_count IN (0, 3);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumesByOption (
    v_cluster_id UUID,
    v_status VARCHAR(32),
    v_option_key VARCHAR(8192),
    v_option_val VARCHAR(8192)
    )
RETURNS SETOF gluster_volumes_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volumes_view
    WHERE cluster_id = v_cluster_id
        AND status = v_status
        AND id IN (
            SELECT volume_id
            FROM gluster_volume_options
            WHERE option_key = v_option_key
                AND option_val = v_option_val
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumesByStatusTypesAndOption (
    v_cluster_id UUID,
    v_status VARCHAR(32),
    v_vol_types TEXT,
    v_option_key VARCHAR(8192),
    v_option_val VARCHAR(8192)
    )
RETURNS SETOF gluster_volumes_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volumes_view
    WHERE cluster_id = v_cluster_id
        AND status = v_status
        AND vol_type IN (
            SELECT ID
            FROM fnSplitter(v_vol_types)
            )
        AND id IN (
            SELECT volume_id
            FROM gluster_volume_options
            WHERE option_key = v_option_key
                AND option_val = v_option_val
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumesByStatusAndTypes (
    v_cluster_id UUID,
    v_status VARCHAR(32),
    v_vol_types TEXT
    )
RETURNS SETOF gluster_volumes_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volumes_view
    WHERE cluster_id = v_cluster_id
        AND status = v_status
        AND vol_type IN (
            SELECT ID
            FROM fnSplitter(v_vol_types)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeById (v_volume_id UUID)
RETURNS SETOF gluster_volumes_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volumes_view
    WHERE id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeByName (
    v_cluster_id UUID,
    v_vol_name VARCHAR(1000)
    )
RETURNS SETOF gluster_volumes_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volumes_view
    WHERE cluster_id = v_cluster_id
        AND vol_name = v_vol_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeByGlusterTaskId (v_task_id UUID)
RETURNS SETOF gluster_volumes_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volumes_view
    WHERE task_id = v_task_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeDetailsById (v_volume_id UUID)
RETURNS SETOF gluster_volume_details STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_details
    WHERE volume_id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterBrickById (v_id UUID)
RETURNS SETOF gluster_volume_bricks_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_bricks_view
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetBricksByGlusterVolumeGuid (v_volume_id UUID)
RETURNS SETOF gluster_volume_bricks_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_bricks_view
    WHERE volume_id = v_volume_id
    ORDER BY brick_order;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterVolumeBricksByServerGuid (v_server_id UUID)
RETURNS SETOF gluster_volume_bricks_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_bricks_view
    WHERE server_id = v_server_id
    ORDER BY brick_order;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetBrickByServerIdAndDirectory (
    v_server_id UUID,
    v_brick_dir VARCHAR(4096)
    )
RETURNS SETOF gluster_volume_bricks_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_bricks_view
    WHERE server_id = v_server_id
        AND brick_dir = v_brick_dir;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetBricksByTaskId (v_task_id UUID)
RETURNS SETOF gluster_volume_bricks_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_bricks_view
    WHERE task_id = v_task_id
    ORDER BY brick_order;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetBrickDetailsById (v_brick_id UUID)
RETURNS SETOF gluster_volume_brick_details STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_brick_details
    WHERE brick_id = v_brick_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterOptionById (v_id UUID)
RETURNS SETOF gluster_volume_options STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_options
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetOptionsByGlusterVolumeGuid (v_volume_id UUID)
RETURNS SETOF gluster_volume_options STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_options
    WHERE volume_id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAccessProtocolsByGlusterVolumeGuid (v_volume_id UUID)
RETURNS SETOF gluster_volume_access_protocols STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_access_protocols
    WHERE volume_id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterTaskByGlusterVolumeGuid (v_volume_id UUID)
RETURNS SETOF gluster_volume_task_steps STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT gluster_volume_task_steps.*
    FROM gluster_volume_task_steps,
        gluster_volumes vol
    WHERE volume_id = v_volume_id
        AND vol.id = volume_id
        AND (
            job_status = 'STARTED'
            OR (
                job_status != 'STARTED'
                AND external_id = vol.task_id
                )
            )
    ORDER BY job_start_time DESC LIMIT 1;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetTransportTypesByGlusterVolumeGuid (v_volume_id UUID)
RETURNS SETOF gluster_volume_transport_types STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_transport_types
    WHERE volume_id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeByGuid (v_volume_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volumes
    WHERE id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumesByGuids (v_volume_ids VARCHAR(5000))
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volumes
    WHERE id IN (
            SELECT *
            FROM fnSplitterUuid(v_volume_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeByName (
    v_cluster_id UUID,
    v_vol_name VARCHAR(1000)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volumes
    WHERE cluster_id = v_cluster_id
        AND vol_name = v_vol_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumesByClusterId (v_cluster_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volumes
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeBrick (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volume_bricks
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeBricks (v_ids VARCHAR(5000))
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volume_bricks
    WHERE id IN (
            SELECT *
            FROM fnSplitterUuid(v_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeOption (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volume_options
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeOptions (v_ids VARCHAR(5000))
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volume_options
    WHERE id IN (
            SELECT *
            FROM fnSplitterUuid(v_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeAccessProtocol (
    v_volume_id UUID,
    v_access_protocol VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volume_access_protocols
    WHERE volume_id = v_volume_id
        AND access_protocol = v_access_protocol;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterVolumeTransportType (
    v_volume_id UUID,
    v_transport_type VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_volume_transport_types
    WHERE volume_id = v_volume_id
        AND transport_type = v_transport_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolume (
    v_id UUID,
    v_cluster_id UUID,
    v_vol_name VARCHAR(1000),
    v_vol_type VARCHAR(32),
    v_status VARCHAR(32),
    v_replica_count INT,
    v_stripe_count INT,
    v_disperse_count INT,
    v_redundancy_count INT,
    v_is_arbiter boolean
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volumes
    SET cluster_id = v_cluster_id,
        vol_name = v_vol_name,
        vol_type = v_vol_type,
        status = v_status,
        replica_count = v_replica_count,
        stripe_count = v_stripe_count,
        disperse_count = v_disperse_count,
        redundancy_count = v_redundancy_count,
        is_arbiter = v_is_arbiter,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeDetails (
    v_volume_id UUID,
    v_total_space BIGINT,
    v_used_space BIGINT,
    v_free_space BIGINT,
    v_confirmed_free_space BIGINT,
    v_vdo_savings INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_details
    SET total_space = v_total_space,
        used_space = v_used_space,
        free_space = v_free_space,
        confirmed_free_space = v_confirmed_free_space,
        vdo_savings = v_vdo_savings,
        _update_date = LOCALTIMESTAMP
    WHERE volume_id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeBrick (
    v_id UUID,
    v_new_id UUID,
    v_new_server_id UUID,
    v_new_brick_dir VARCHAR(4096),
    v_new_status VARCHAR(32),
    v_new_network_id UUID,
    v_is_arbiter boolean
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE FROM gluster_volume_brick_details
    WHERE brick_id = v_id;
    UPDATE gluster_volume_bricks
    SET id = v_new_id,
        server_id = v_new_server_id,
        brick_dir = v_new_brick_dir,
        status = v_new_status,
        network_id = v_new_network_id,
        is_arbiter = v_is_arbiter,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeBrickDetails (
    v_brick_id UUID,
    v_total_space BIGINT,
    v_used_space BIGINT,
    v_free_space BIGINT,
    v_confirmed_free_space BIGINT,
    v_confirmed_total_space BIGINT,
    v_vdo_savings INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_brick_details
    SET total_space = v_total_space,
        used_space = v_used_space,
        free_space = v_free_space,
        confirmed_free_space = v_confirmed_free_space,
        confirmed_total_space = v_confirmed_total_space,
        vdo_savings = v_vdo_savings,
        _update_date = LOCALTIMESTAMP
    WHERE brick_id = v_brick_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeBrickStatus (
    v_id UUID,
    v_status VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_bricks
    SET status = v_status,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeBrickOrder (
    v_id UUID,
    v_brick_order INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_bricks
    SET brick_order = v_brick_order,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeBrickNetworkId (
    v_id UUID,
    v_network_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_bricks
    SET network_id = v_network_id,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeStatus (
    v_volume_id UUID,
    v_status VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volumes
    SET status = v_status,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeAsyncTask (
    v_volume_id UUID,
    v_task_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volumes
    SET task_id = v_task_id,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeBrickAsyncTask (
    v_id UUID,
    v_task_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_bricks
    SET task_id = v_task_id,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterBrickTaskByServerIdBrickDir (
    v_server_id UUID,
    v_brick_dir VARCHAR(200),
    v_task_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_bricks
    SET task_id = v_task_id,
        _update_date = LOCALTIMESTAMP
    WHERE server_id = v_server_id
        AND brick_dir = v_brick_dir;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeBrickUnSyncedEntries (
    v_id UUID,
    v_unsynced_entries integer,
    v_unsynced_entries_history text
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_bricks
    SET unsynced_entries = v_unsynced_entries,
        unsynced_entries_history = v_unsynced_entries_history,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeStatusByName (
    v_cluster_id UUID,
    v_vol_name VARCHAR(1000),
    v_status VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volumes
    SET status = v_status,
        _update_date = LOCALTIMESTAMP
    WHERE cluster_id = v_cluster_id
        AND vol_name = v_vol_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterVolumeOption (
    v_id UUID,
    v_option_val VARCHAR(8192)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volume_options
    SET option_val = v_option_val
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateReplicaCount (
    v_volume_id UUID,
    v_replica_count INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_volumes
    SET replica_count = v_replica_count,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_volume_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetBricksByClusterIdAndNetworkId (
    v_cluster_id UUID,
    v_network_id UUID
    )
RETURNS SETOF gluster_volume_bricks_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_volume_bricks_view
    WHERE network_id = v_network_id
        AND cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



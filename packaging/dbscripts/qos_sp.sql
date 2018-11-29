

----------------------------------------------------------------
-- [qos] Table
----------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertStorageQos (
    v_id uuid,
    v_qos_type SMALLINT,
    v_name VARCHAR(50),
    v_description TEXT,
    v_storage_pool_id uuid,
    v_max_throughput INT,
    v_max_read_throughput INT,
    v_max_write_throughput INT,
    v_max_iops INT,
    v_max_read_iops INT,
    v_max_write_iops INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO qos (
        id,
        qos_type,
        name,
        description,
        storage_pool_id,
        max_throughput,
        max_read_throughput,
        max_write_throughput,
        max_iops,
        max_read_iops,
        max_write_iops
        )
    VALUES (
        v_id,
        v_qos_type,
        v_name,
        v_description,
        v_storage_pool_id,
        v_max_throughput,
        v_max_read_throughput,
        v_max_write_throughput,
        v_max_iops,
        v_max_read_iops,
        v_max_write_iops
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertCpuQos (
    v_id uuid,
    v_qos_type SMALLINT,
    v_name VARCHAR(50),
    v_description TEXT,
    v_storage_pool_id uuid,
    v_cpu_limit INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO qos (
        id,
        qos_type,
        name,
        description,
        storage_pool_id,
        cpu_limit
        )
    VALUES (
        v_id,
        v_qos_type,
        v_name,
        v_description,
        v_storage_pool_id,
        v_cpu_limit
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertNetworkQos (
    v_id uuid,
    v_qos_type SMALLINT,
    v_name VARCHAR(50),
    v_description TEXT,
    v_storage_pool_id uuid,
    v_inbound_average INT,
    v_inbound_peak INT,
    v_inbound_burst INT,
    v_outbound_average INT,
    v_outbound_peak INT,
    v_outbound_burst INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO qos (
        id,
        qos_type,
        name,
        description,
        storage_pool_id,
        inbound_average,
        inbound_peak,
        inbound_burst,
        outbound_average,
        outbound_peak,
        outbound_burst
        )
    VALUES (
        v_id,
        v_qos_type,
        v_name,
        v_description,
        v_storage_pool_id,
        v_inbound_average,
        v_inbound_peak,
        v_inbound_burst,
        v_outbound_average,
        v_outbound_peak,
        v_outbound_burst
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertHostNetworkQos (
    v_id uuid,
    v_qos_type SMALLINT,
    v_name VARCHAR(50),
    v_description TEXT,
    v_storage_pool_id uuid,
    v_out_average_linkshare INT,
    v_out_average_upperlimit INT,
    v_out_average_realtime INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO qos (
        id,
        qos_type,
        name,
        description,
        storage_pool_id,
        out_average_linkshare,
        out_average_upperlimit,
        out_average_realtime
        )
    VALUES (
        v_id,
        v_qos_type,
        v_name,
        v_description,
        v_storage_pool_id,
        v_out_average_linkshare,
        v_out_average_upperlimit,
        v_out_average_realtime
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateStorageQos (
    v_id uuid,
    v_qos_type SMALLINT,
    v_name VARCHAR(50),
    v_description TEXT,
    v_storage_pool_id uuid,
    v_max_throughput INT,
    v_max_read_throughput INT,
    v_max_write_throughput INT,
    v_max_iops INT,
    v_max_read_iops INT,
    v_max_write_iops INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE qos
    SET qos_type = v_qos_type,
        name = v_name,
        description = v_description,
        storage_pool_id = v_storage_pool_id,
        max_throughput = v_max_throughput,
        max_read_throughput = v_max_read_throughput,
        max_write_throughput = v_max_write_throughput,
        max_iops = v_max_iops,
        max_read_iops = v_max_read_iops,
        max_write_iops = v_max_write_iops,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateCpuQos (
    v_id uuid,
    v_qos_type SMALLINT,
    v_name VARCHAR(50),
    v_description TEXT,
    v_storage_pool_id uuid,
    v_cpu_limit INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE qos
    SET qos_type = v_qos_type,
        name = v_name,
        description = v_description,
        storage_pool_id = v_storage_pool_id,
        cpu_limit = v_cpu_limit,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateNetworkQos (
    v_id uuid,
    v_qos_type SMALLINT,
    v_name VARCHAR(50),
    v_description TEXT,
    v_storage_pool_id uuid,
    v_inbound_average INT,
    v_inbound_peak INT,
    v_inbound_burst INT,
    v_outbound_average INT,
    v_outbound_peak INT,
    v_outbound_burst INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE qos
    SET qos_type = v_qos_type,
        name = v_name,
        description = v_description,
        storage_pool_id = v_storage_pool_id,
        inbound_average = v_inbound_average,
        inbound_peak = v_inbound_peak,
        inbound_burst = v_inbound_burst,
        outbound_average = v_outbound_average,
        outbound_peak = v_outbound_peak,
        outbound_burst = v_outbound_burst,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateHostNetworkQos (
    v_id uuid,
    v_qos_type SMALLINT,
    v_name VARCHAR(50),
    v_description TEXT,
    v_storage_pool_id uuid,
    v_out_average_linkshare INT,
    v_out_average_upperlimit INT,
    v_out_average_realtime INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE qos
    SET qos_type = v_qos_type,
        name = v_name,
        description = v_description,
        storage_pool_id = v_storage_pool_id,
        out_average_linkshare = v_out_average_linkshare,
        out_average_upperlimit = v_out_average_upperlimit,
        out_average_realtime = v_out_average_realtime,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteQos (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM qos
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetQosByQosId (v_id UUID)
RETURNS SETOF qos STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM qos
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllQosForStoragePoolByQosType (
    v_storage_pool_id UUID,
    v_qos_type SMALLINT
    )
RETURNS SETOF qos STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM qos
    WHERE storage_pool_id = v_storage_pool_id
        AND qos_type = v_qos_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllQosForStoragePool (v_storage_pool_id UUID)
RETURNS SETOF qos STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM qos
    WHERE storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllQosByQosType (v_qos_type SMALLINT)
RETURNS SETOF qos STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM qos
    WHERE qos_type = v_qos_type;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllStorageQos (v_user_id UUID)
RETURNS SETOF qos STABLE AS $PROCEDURE$
BEGIN
   RETURN QUERY SELECT *
      FROM qos WHERE
        qos_type = 1 AND
        EXISTS (
            SELECT 1
            FROM user_storage_domain_permissions_view
            INNER JOIN storage_pool
                ON storage_pool.id = user_storage_domain_permissions_view.entity_id
            WHERE storage_pool.id = qos.storage_pool_id
                AND user_storage_domain_permissions_view.user_id = v_user_id
        );

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllCpuQos (v_user_id UUID)
RETURNS SETOF qos STABLE AS $PROCEDURE$
BEGIN
   RETURN QUERY SELECT *
      FROM qos WHERE
        qos_type = 2 AND
        EXISTS (
            SELECT 1
            FROM cpu_profiles
            INNER JOIN user_cluster_permissions_view
                ON cpu_profiles.cluster_id = user_cluster_permissions_view.entity_id
            WHERE cpu_profiles.qos_id = qos.id
                AND user_cluster_permissions_view.user_id = v_user_id
        );

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllNetworkQos (v_user_id UUID)
RETURNS SETOF qos STABLE AS $PROCEDURE$
BEGIN
   RETURN QUERY SELECT *
      FROM qos WHERE
        qos_type = 3 AND
        EXISTS (
            SELECT 1
            FROM user_vnic_profile_permissions_view
            INNER JOIN vnic_profiles_view
                ON vnic_profiles_view.id = user_vnic_profile_permissions_view.entity_id
            WHERE vnic_profiles_view.network_qos_id = qos.id
                AND user_vnic_profile_permissions_view.user_id = v_user_id
        );

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllHostNetworkQos ()
RETURNS SETOF qos STABLE AS $PROCEDURE$
BEGIN
   RETURN QUERY SELECT *
      FROM qos WHERE
        qos_type = 4 AND
        EXISTS (
            SELECT 1
            FROM user_network_permissions_view
            INNER JOIN network_view
                ON network_view.id = user_network_permissions_view.entity_id
            WHERE network_view.qos_id = qos.id
                AND user_network_permissions_view.user_id = v_user_id
        );

END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetQosByDiskProfiles (v_disk_profile_ids UUID[])
RETURNS SETOF qos_for_disk_profile_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM qos_for_disk_profile_view
    WHERE disk_profile_id = ANY(v_disk_profile_ids);

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetQosByVmIds (v_vm_ids UUID[])
RETURNS SETOF qos_for_vm_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM qos_for_vm_view
    WHERE vm_id = ANY(v_vm_ids);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetHostNetworkQosOfMigrationNetworkByClusterId (v_cluster_id UUID)
RETURNS SETOF host_network_qos_of_migration_network_by_cluster STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM host_network_qos_of_migration_network_by_cluster
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



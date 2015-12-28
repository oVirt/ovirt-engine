

/* ----------------------------------------------------------------
 Stored procedures for database operations on Services
 related tables:
      - gluster_service_types
      - gluster_services
      - gluster_cluster_services
      - gluster_server_services
----------------------------------------------------------------*/
-- Fetch all gluster service types
CREATE OR REPLACE FUNCTION GetGlusterServiceTypes ()
RETURNS SETOF gluster_service_types STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_service_types;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch services of a given type
CREATE OR REPLACE FUNCTION GetGlusterServicesByType (v_service_type VARCHAR(100))
RETURNS SETOF gluster_services STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_services
    WHERE service_type = v_service_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch services of given cluster
CREATE OR REPLACE FUNCTION GetGlusterClusterServicesByClusterId (v_cluster_id UUID)
RETURNS SETOF gluster_cluster_services STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_cluster_services
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch cluster-wide service given cluster id and service type
CREATE OR REPLACE FUNCTION GetGlusterClusterServicesByClusterIdAndServiceType (
    v_cluster_id UUID,
    v_service_type VARCHAR(100)
    )
RETURNS SETOF gluster_cluster_services STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_cluster_services
    WHERE cluster_id = v_cluster_id
        AND service_type = v_service_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch services of given server
CREATE OR REPLACE FUNCTION GetGlusterServerServicesByServerId (v_server_id UUID)
RETURNS SETOF gluster_server_services_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_server_services_view
    WHERE server_id = v_server_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch services from all servers of given cluster
CREATE OR REPLACE FUNCTION GetGlusterServerServicesByClusterId (v_cluster_id UUID)
RETURNS SETOF gluster_server_services_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT s.*
    FROM gluster_server_services_view s,
        vds_static v
    WHERE s.server_id = v.vds_id
        AND v.cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch services of given cluster
CREATE OR REPLACE FUNCTION GetGlusterServerServicesByClusterIdAndServiceType (
    v_cluster_id UUID,
    v_service_type VARCHAR(100)
    )
RETURNS SETOF gluster_server_services_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT s.*
    FROM gluster_server_services_view s,
        vds_static v
    WHERE s.server_id = v.vds_id
        AND v.cluster_id = v_cluster_id
        AND s.service_type = v_service_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch services of given server
CREATE OR REPLACE FUNCTION GetGlusterServerServicesByServerIdAndServiceType (
    v_server_id UUID,
    v_service_type VARCHAR(100)
    )
RETURNS SETOF gluster_server_services_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_server_services_view
    WHERE server_id = v_server_id
        AND service_type = v_service_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch a service by it's ID
CREATE OR REPLACE FUNCTION GetGlusterServiceByGlusterServiceId (v_id UUID)
RETURNS SETOF gluster_services STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_services
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch a server service by it's ID
CREATE OR REPLACE FUNCTION GetGlusterServerServiceByGlusterServerServiceId (v_id UUID)
RETURNS SETOF gluster_server_services_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_server_services_view
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch all server services
CREATE OR REPLACE FUNCTION GetAllFromGlusterServerServices ()
RETURNS SETOF gluster_server_services_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_server_services_view;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch a service by it's name
CREATE OR REPLACE FUNCTION GetGlusterServiceByTypeAndName (
    v_service_type VARCHAR(100),
    v_service_name VARCHAR(100)
    )
RETURNS SETOF gluster_services STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_services
    WHERE service_type = v_service_type
        AND service_name = v_service_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Fetch all services
CREATE OR REPLACE FUNCTION GetAllFromGlusterServices ()
RETURNS SETOF gluster_services STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_services;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Insert a cluster-wide service type
CREATE OR REPLACE FUNCTION InsertGlusterClusterService (
    v_cluster_id UUID,
    v_service_type VARCHAR(100),
    v_status VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_cluster_services (
        cluster_id,
        service_type,
        status
        )
    VALUES (
        v_cluster_id,
        v_service_type,
        v_status
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Insert a server specific service
CREATE OR REPLACE FUNCTION InsertGlusterServerService (
    v_id UUID,
    v_server_id UUID,
    v_service_id UUID,
    v_pid INT,
    v_status VARCHAR(32),
    v_message VARCHAR(1000)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_server_services (
        id,
        server_id,
        service_id,
        pid,
        status,
        message
        )
    VALUES (
        v_id,
        v_server_id,
        v_service_id,
        v_pid,
        v_status,
        v_message
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Update status of a cluster-wide service type
CREATE OR REPLACE FUNCTION UpdateGlusterClusterService (
    v_cluster_id UUID,
    v_service_type VARCHAR(100),
    v_status VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_cluster_services
    SET status = v_status,
        _update_date = LOCALTIMESTAMP
    WHERE cluster_id = v_cluster_id
        AND service_type = v_service_type;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Update a server specific service
CREATE OR REPLACE FUNCTION UpdateGlusterServerService (
    v_id UUID,
    v_pid INT,
    v_status VARCHAR(32),
    v_message VARCHAR(1000)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_server_services
    SET pid = v_pid,
        status = v_status,
        message = v_message,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Update a server specific service by server id and service id
CREATE OR REPLACE FUNCTION UpdateGlusterServerServiceByServerIdAndServiceType (
    v_server_id UUID,
    v_service_id UUID,
    v_pid INT,
    v_status VARCHAR(32),
    v_message VARCHAR(1000)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_server_services
    SET pid = v_pid,
        status = v_status,
        message = v_message,
        _update_date = LOCALTIMESTAMP
    WHERE server_id = v_server_id
        AND service_id = v_service_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- Delete a server specific service
CREATE OR REPLACE FUNCTION DeleteGlusterServerService (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_server_services
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



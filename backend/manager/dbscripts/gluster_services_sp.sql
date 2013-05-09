/* ----------------------------------------------------------------
 Stored procedures for database operations on Services
 related tables:
      - gluster_service_types
      - gluster_services
      - gluster_cluster_services
      - gluster_server_services
----------------------------------------------------------------*/

-- Fetch all gluster service types
Create or replace FUNCTION GetGlusterServiceTypes()
    RETURNS SETOF gluster_service_types
    AS $procedure$
BEGIN
    RETURN  QUERY SELECT *
    FROM    gluster_service_types;
END; $procedure$
LANGUAGE plpgsql;

-- Fetch services of a given type
Create or replace FUNCTION GetGlusterServicesByType(v_service_type VARCHAR(100))
    RETURNS SETOF gluster_services
    AS $procedure$
BEGIN
    RETURN  QUERY SELECT *
    FROM    gluster_services
    WHERE   service_type = v_service_type;
END; $procedure$
LANGUAGE plpgsql;

-- Fetch services of given cluster
Create or replace FUNCTION GetGlusterClusterServicesByClusterId(v_cluster_id UUID)
    RETURNS SETOF gluster_cluster_services
    AS $procedure$
BEGIN
    RETURN  QUERY SELECT *
    FROM    gluster_cluster_services
    WHERE   cluster_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;

-- Fetch cluster-wide service given cluster id and service type
Create or replace FUNCTION GetGlusterClusterServicesByClusterIdAndServiceType(v_cluster_id UUID,
                                                                v_service_type VARCHAR(100))
    RETURNS SETOF gluster_cluster_services
    AS $procedure$
BEGIN
    RETURN  QUERY SELECT *
    FROM    gluster_cluster_services
    WHERE   cluster_id = v_cluster_id
    AND     service_type = v_service_type;
END; $procedure$
LANGUAGE plpgsql;

-- Fetch services of given server
Create or replace FUNCTION GetGlusterServerServicesByServerId(v_server_id UUID)
    RETURNS SETOF gluster_server_services_view
    AS $procedure$
BEGIN
    RETURN  QUERY SELECT *
    FROM    gluster_server_services_view
    WHERE   server_id = v_server_id;
END; $procedure$
LANGUAGE plpgsql;

-- Fetch services from all servers of given cluster
Create or replace FUNCTION GetGlusterServerServicesByClusterId(v_cluster_id UUID)
    RETURNS SETOF gluster_server_services_view
    AS $procedure$
BEGIN
    RETURN  QUERY SELECT s.*
    FROM    gluster_server_services_view s, vds_static v
    WHERE   s.server_id = v.vds_id
    AND     v.vds_group_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;

-- Fetch a service by it's ID
Create or replace FUNCTION GetGlusterServiceByGlusterServiceId(v_id UUID)
RETURNS SETOF gluster_services
AS $procedure$
BEGIN
    RETURN  QUERY SELECT *
    FROM    gluster_services
    WHERE   id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- Fetch a server service by it's ID
Create or replace FUNCTION GetGlusterServerServiceByGlusterServerServiceId(v_id UUID)
RETURNS SETOF gluster_server_services_view
AS $procedure$
BEGIN
    RETURN  QUERY SELECT *
    FROM    gluster_server_services_view
    WHERE   id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- Fetch all server services
Create or replace FUNCTION GetAllFromGlusterServerServices()
RETURNS SETOF gluster_server_services_view
AS $procedure$
BEGIN
    RETURN  QUERY SELECT *
    FROM    gluster_server_services_view;
END; $procedure$
LANGUAGE plpgsql;

-- Fetch a service by it's name
Create or replace FUNCTION GetGlusterServiceByTypeAndName(v_service_type VARCHAR(100),
                                                        v_service_name VARCHAR(100))
RETURNS SETOF gluster_services
AS $procedure$
BEGIN
    RETURN  QUERY SELECT *
    FROM    gluster_services
    WHERE   service_type = v_service_type
    AND     service_name = v_service_name;
END; $procedure$
LANGUAGE plpgsql;

-- Fetch all services
Create or replace FUNCTION GetAllFromGlusterServices()
RETURNS SETOF gluster_services
AS $procedure$
BEGIN
    RETURN QUERY SELECT * FROM gluster_services;
END; $procedure$
LANGUAGE plpgsql;

-- Insert a cluster-wide service type
Create or replace FUNCTION InsertGlusterClusterService(v_cluster_id UUID,
                                                v_service_type VARCHAR(100),
                                                v_status VARCHAR(32))
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO gluster_cluster_services(cluster_id, service_type, status)
    VALUES (v_cluster_id, v_service_type, v_status);
END; $procedure$
LANGUAGE plpgsql;

-- Insert a server specific service
Create or replace FUNCTION InsertGlusterServerService(v_id UUID,
                                                v_server_id UUID,
                                                v_service_id UUID,
                                                v_pid INTEGER,
                                                v_status VARCHAR(32),
                                                v_message VARCHAR(1000))
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO gluster_server_services(id, server_id, service_id, pid, status, message)
    VALUES (v_id, v_server_id, v_service_id, v_pid, v_status, v_message);
END; $procedure$
LANGUAGE plpgsql;

-- Update status of a cluster-wide service type
Create or replace FUNCTION UpdateGlusterClusterService(v_cluster_id UUID,
                                                    v_service_type VARCHAR(100),
                                                    v_status VARCHAR(32))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE  gluster_cluster_services
    SET     status = v_status,
            _update_date = LOCALTIMESTAMP
    WHERE   cluster_id = v_cluster_id
    AND     service_type = v_service_type;
END; $procedure$
LANGUAGE plpgsql;

-- Update a server specific service
Create or replace FUNCTION UpdateGlusterServerService(v_id UUID,
                                                v_pid INTEGER,
                                                v_status VARCHAR(32),
                                                v_message VARCHAR(1000))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE  gluster_server_services
    SET     pid = v_pid,
            status = v_status,
            message = v_message,
            _update_date = LOCALTIMESTAMP
    WHERE   id = v_id;
END; $procedure$
LANGUAGE plpgsql;

-- Delete a server specific service
Create or replace FUNCTION DeleteGlusterServerService(v_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM gluster_server_services
    WHERE   id = v_id;
END; $procedure$
LANGUAGE plpgsql;

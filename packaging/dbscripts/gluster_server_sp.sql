/*--------------------------------------------------------------
Stored procedures for database operations on gluster_server table
--------------------------------------------------------------*/

Create or replace FUNCTION InsertGlusterServer(v_server_id UUID,
                                            v_gluster_server_uuid UUID)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO gluster_server(server_id, gluster_server_uuid)
    VALUES (v_server_id, v_gluster_server_uuid);
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetGlusterServerByServerId(v_server_id UUID)
RETURNS SETOF gluster_server STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM gluster_server
    WHERE server_id = v_server_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetGlusterServerByGlusterServerUUID(v_gluster_server_uuid UUID)
RETURNS SETOF gluster_server STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM gluster_server
    WHERE gluster_server_uuid = v_gluster_server_uuid;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION DeleteGlusterServer(v_server_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM gluster_server
    WHERE server_id = v_server_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION DeleteGlusterServerByGlusterServerUUID(v_gluster_server_uuid UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM gluster_server
    WHERE gluster_server_uuid = v_gluster_server_uuid;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION UpdateGlusterServer(v_server_id UUID,
                                            v_gluster_server_uuid UUID)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_server
    SET gluster_server_uuid = v_gluster_server_uuid
    WHERE server_id = v_server_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateGlusterServerKnownAddresses(v_server_id UUID,
                                            v_known_addresses VARCHAR(250))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_server
    SET known_addresses = v_known_addresses
    WHERE server_id = v_server_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION AddGlusterServerKnownAddress(v_server_id UUID,
                                            v_known_address VARCHAR(250))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_server
    SET known_addresses = coalesce(known_addresses || ',', '') || v_known_address
    WHERE server_id = v_server_id;
END; $procedure$
LANGUAGE plpgsql;



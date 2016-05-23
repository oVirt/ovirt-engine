

/*--------------------------------------------------------------
Stored procedures for database operations on gluster_server table
--------------------------------------------------------------*/
CREATE OR REPLACE FUNCTION InsertGlusterServer (
    v_server_id UUID,
    v_gluster_server_uuid UUID,
    v_peer_status VARCHAR(20)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_server (
        server_id,
        gluster_server_uuid,
        peer_status
        )
    VALUES (
        v_server_id,
        v_gluster_server_uuid,
        v_peer_status
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterServerByServerId (v_server_id UUID)
RETURNS SETOF gluster_server STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_server
    WHERE server_id = v_server_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterServerByGlusterServerUUID (v_gluster_server_uuid UUID)
RETURNS SETOF gluster_server STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_server
    WHERE gluster_server_uuid = v_gluster_server_uuid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterServer (v_server_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_server
    WHERE server_id = v_server_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterServerByGlusterServerUUID (v_gluster_server_uuid UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_server
    WHERE gluster_server_uuid = v_gluster_server_uuid;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterServer (
    v_server_id UUID,
    v_gluster_server_uuid UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_server
    SET gluster_server_uuid = v_gluster_server_uuid
    WHERE server_id = v_server_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterServerKnownAddresses (
    v_server_id UUID,
    v_known_addresses VARCHAR(250)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_server
    SET known_addresses = v_known_addresses
    WHERE server_id = v_server_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterServerPeerStatus (
    v_server_id UUID,
    v_peer_status VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_server
    SET peer_status = v_peer_status
    WHERE server_id = v_server_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION AddGlusterServerKnownAddress (
    v_server_id UUID,
    v_known_address VARCHAR(250)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_server
    SET known_addresses = coalesce(known_addresses || ',', '') || v_known_address
    WHERE server_id = v_server_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



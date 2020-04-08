-- Adds a new sso client according to the specified arguments
CREATE OR REPLACE FUNCTION sso_oauth_register_client (
    v_client_id VARCHAR(128),
    v_client_secret VARCHAR(1024),
    v_scope VARCHAR(1024),
    v_certificate_location VARCHAR(1024) DEFAULT NULL,
    v_callback_prefix VARCHAR(1024) DEFAULT NULL,
    v_description TEXT DEFAULT 'oVirt Engine',
    v_email VARCHAR(256) DEFAULT '',
    v_encrypted_userinfo BOOLEAN DEFAULT TRUE,
    v_trusted BOOLEAN DEFAULT TRUE,
    v_notification_callback VARCHAR(1024) DEFAULT NULL,
    v_notification_callback_protocol VARCHAR(32) DEFAULT 'TLSv1',
    v_notification_callback_verify_host BOOLEAN DEFAULT TRUE,
    v_notification_callback_verify_chain BOOLEAN DEFAULT TRUE
    )
RETURNS VOID AS $PROCEDURE$

BEGIN
    -- Adding the sso client
    INSERT INTO sso_clients (
        client_id,
        client_secret,
        scope,
        certificate_location,
        callback_prefix,
        description,
        email,
        encrypted_userinfo,
        trusted,
        notification_callback,
        notification_callback_protocol,
        notification_callback_verify_host,
        notification_callback_verify_chain
        )
    VALUES (
        v_client_id,
        v_client_secret,
        v_scope,
        v_certificate_location,
        v_callback_prefix,
        v_description,
        v_email,
        v_encrypted_userinfo,
        v_trusted,
        v_notification_callback,
        v_notification_callback_protocol,
        v_notification_callback_verify_host,
        v_notification_callback_verify_chain
        );
END;$PROCEDURE$

LANGUAGE plpgsql;

-- Deletes a sso client by client_id
CREATE OR REPLACE FUNCTION sso_oauth_unregister_client (v_client_id VARCHAR(128))
RETURNS VOID AS $PROCEDURE$

BEGIN
    -- Removing the sso client
    DELETE
    FROM sso_clients
    WHERE client_id = v_client_id;
END;$PROCEDURE$

LANGUAGE plpgsql;

-- Checks if the sso client with id exists.
CREATE OR REPLACE FUNCTION sso_oauth_client_exists (v_client_id VARCHAR(128))
RETURNS SETOF INT IMMUTABLE AS $PROCEDURE$

BEGIN
    IF EXISTS (
            SELECT 1
            FROM sso_clients
            WHERE client_id = v_client_id
            ) THEN
        RETURN QUERY
        SELECT 1;
    ELSE

        RETURN QUERY
        SELECT 0;
    END IF;

END;$PROCEDURE$

LANGUAGE plpgsql;

-- Returns the Client by client Id
CREATE OR REPLACE FUNCTION get_oauth_client (v_client_id VARCHAR(128))
RETURNS SETOF sso_clients STABLE AS $PROCEDURE$

BEGIN
    RETURN QUERY

    SELECT *
    FROM sso_clients
    WHERE client_id = v_client_id;
END;$PROCEDURE$

LANGUAGE plpgsql;

-- Updates the client info for the given client
CREATE OR REPLACE FUNCTION update_oauth_client (
    v_client_id VARCHAR(128),
    v_scope VARCHAR(1024),
    v_certificate_location VARCHAR(1024) DEFAULT NULL,
    v_callback_prefix VARCHAR(1024) DEFAULT NULL,
    v_description TEXT DEFAULT 'oVirt Engine',
    v_email VARCHAR(256) DEFAULT '',
    v_encrypted_userinfo BOOLEAN DEFAULT TRUE,
    v_trusted BOOLEAN DEFAULT TRUE,
    v_notification_callback VARCHAR(1024) DEFAULT NULL
    )
RETURNS VOID AS $PROCEDURE$

BEGIN
    UPDATE sso_clients
    SET scope = v_scope,
        certificate_location = v_certificate_location,
        callback_prefix = v_callback_prefix,
        description = v_description,
        email = v_email,
        encrypted_userinfo = v_encrypted_userinfo,
        trusted = v_trusted,
        notification_callback = v_notification_callback
    WHERE client_id = v_client_id;
END;$PROCEDURE$

LANGUAGE plpgsql;

-- Updates the client callback prefix for the given client
CREATE OR REPLACE FUNCTION update_oauth_client_callback_prefix (
    v_client_id VARCHAR(128),
    v_callback_prefix VARCHAR(1024) DEFAULT NULL
    )
RETURNS VOID AS $PROCEDURE$

BEGIN
    UPDATE sso_clients
    SET callback_prefix = v_callback_prefix
    WHERE client_id = v_client_id;
END;$PROCEDURE$

LANGUAGE plpgsql;

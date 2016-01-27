

----------------------------------------------------------------
-- [engine_sessions] Table
--
CREATE OR REPLACE FUNCTION InsertEngineSession (
    INOUT v_id INT,
    v_engine_session_id TEXT,
    v_user_id UUID,
    v_user_name VARCHAR(255),
    v_authz_name VARCHAR(255),
    v_source_ip VARCHAR(50),
    v_group_ids VARCHAR(2048),
    v_role_ids VARCHAR(2048)
    )
RETURNS INT AS $PROCEDURE$
BEGIN
    INSERT INTO engine_sessions (
        engine_session_id,
        user_id,
        user_name,
        authz_name,
        source_ip,
        group_ids,
        role_ids
        )
    VALUES (
        v_engine_session_id,
        v_user_id,
        v_user_name,
        v_authz_name,
        v_source_ip,
        v_group_ids,
        v_role_ids
        );

    v_id := CURRVAL('engine_session_seq');
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetEngineSession (v_id INT)
RETURNS SETOF engine_sessions STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM engine_sessions
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetEngineSessionBySessionId (v_engine_session_id TEXT)
RETURNS SETOF engine_sessions STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM engine_sessions
    WHERE engine_session_id = v_engine_session_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteEngineSession (v_id INT)
RETURNS INT AS $PROCEDURE$
DECLARE deleted_rows INT;

BEGIN
    DELETE
    FROM engine_sessions
    WHERE id = v_id;

    GET DIAGNOSTICS deleted_rows = ROW_COUNT;

    RETURN deleted_rows;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllFromEngineSessions ()
RETURNS INT AS $PROCEDURE$
DECLARE deleted_rows INT;

BEGIN
    DELETE
    FROM engine_sessions;

    GET DIAGNOSTICS deleted_rows = ROW_COUNT;

    RETURN deleted_rows;
END;$PROCEDURE$
LANGUAGE plpgsql;





/* ----------------------------------------------------------------
 Stored procedures for database operations on Gluster Hooks
 related tables:
      - gluster_hooks
      - gluster_server_hooks
----------------------------------------------------------------*/
CREATE OR REPLACE FUNCTION InsertGlusterHook (
    v_id UUID,
    v_cluster_id UUID,
    v_gluster_command VARCHAR(128),
    v_stage VARCHAR(50),
    v_name VARCHAR(256),
    v_hook_status VARCHAR(50),
    v_content_type VARCHAR(50),
    v_checksum VARCHAR(256),
    v_content TEXT,
    v_conflict_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_hooks (
        id,
        cluster_id,
        gluster_command,
        stage,
        name,
        hook_status,
        content_type,
        checksum,
        content,
        conflict_status
        )
    VALUES (
        v_id,
        v_cluster_id,
        v_gluster_command,
        v_stage,
        v_name,
        v_hook_status,
        v_content_type,
        v_checksum,
        v_content,
        v_conflict_status
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterHookById (
    v_id UUID,
    v_includeContent BOOLEAN = false
    )
RETURNS SETOF gluster_hooks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT id,
        cluster_id,
        gluster_command,
        stage,
        name,
        hook_status,
        content_type,
        checksum,
        CASE v_includeContent
            WHEN true
                THEN content
            ELSE NULL::TEXT
            END AS content,
        conflict_status,
        _create_date,
        _update_date
    FROM gluster_hooks
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterHookContentById (v_id UUID)
RETURNS SETOF TEXT STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT content
    FROM gluster_hooks
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterHooksByClusterId (v_cluster_id UUID)
RETURNS SETOF gluster_hooks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT id,
        cluster_id,
        gluster_command,
        stage,
        name,
        hook_status,
        content_type,
        checksum,
        NULL::TEXT AS content,
        conflict_status,
        _create_date,
        _update_date
    FROM gluster_hooks
    WHERE cluster_id = v_cluster_id
    ORDER BY gluster_command ASC,
        stage ASC;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterServerHooksById (v_id UUID)
RETURNS SETOF gluster_server_hooks_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_server_hooks_view
    WHERE hook_id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterHook (
    v_cluster_id UUID,
    v_gluster_command VARCHAR(1000),
    v_stage VARCHAR(100),
    v_name VARCHAR(1000),
    v_includeContent BOOLEAN = false
    )
RETURNS SETOF gluster_hooks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT id,
        cluster_id,
        gluster_command,
        stage,
        name,
        hook_status,
        content_type,
        checksum,
        CASE v_includeContent
            WHEN true
                THEN content
            ELSE NULL::TEXT
            END AS content,
        conflict_status,
        _create_date,
        _update_date
    FROM gluster_hooks
    WHERE cluster_id = v_cluster_id
        AND gluster_command = v_gluster_command
        AND stage = v_stage
        AND name = v_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterHookById (v_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_hooks
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterHook (
    v_cluster_id UUID,
    v_gluster_command VARCHAR(1000),
    v_stage VARCHAR(100),
    v_name VARCHAR(1000)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_hooks
    WHERE cluster_id = v_cluster_id
        AND gluster_command = v_gluster_command
        AND stage = v_stage
        AND name = v_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllGlusterHooks (v_cluster_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_hooks
    WHERE cluster_id = v_cluster_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterHooksByIds (v_ids TEXT)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_hooks
    WHERE id IN (
            SELECT *
            FROM fnSplitterUuid(v_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterHookConflictStatus (
    v_id UUID,
    v_conflict_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_hooks
    SET conflict_status = v_conflict_status,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterHookContentType (
    v_id UUID,
    v_content_type VARCHAR(100)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_hooks
    SET content_type = v_content_type,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterHookContent (
    v_id UUID,
    v_checksum VARCHAR(256),
    v_content TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_hooks
    SET checksum = v_checksum,
        content = v_content,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterHook (
    v_id UUID,
    v_hook_status VARCHAR(50),
    v_content_type VARCHAR(50),
    v_checksum VARCHAR(256),
    v_content TEXT,
    v_conflict_status INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_hooks
    SET hook_status = v_hook_status,
        content_type = v_content_type,
        checksum = v_checksum,
        content = v_content,
        conflict_status = v_conflict_status,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterHookStatus (
    v_id UUID,
    v_hook_status VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_hooks
    SET hook_status = v_hook_status,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertGlusterServerHook (
    v_hook_id UUID,
    v_server_id UUID,
    v_hook_status VARCHAR(50),
    v_content_type VARCHAR(50),
    v_checksum VARCHAR(256)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO gluster_server_hooks (
        hook_id,
        server_id,
        hook_status,
        content_type,
        checksum
        )
    VALUES (
        v_hook_id,
        v_server_id,
        v_hook_status,
        v_content_type,
        v_checksum
        );

    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id = v_hook_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetGlusterServerHook (
    v_hook_id UUID,
    v_server_id UUID
    )
RETURNS SETOF gluster_server_hooks_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM gluster_server_hooks_view
    WHERE hook_id = v_hook_id
        AND server_id = v_server_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterServerHook (
    v_hook_id UUID,
    v_server_id UUID,
    v_hook_status VARCHAR(50),
    v_content_type VARCHAR(50),
    v_checksum VARCHAR(256)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_server_hooks
    SET hook_status = v_hook_status,
        content_type = v_content_type,
        checksum = v_checksum,
        _update_date = LOCALTIMESTAMP
    WHERE hook_id = v_hook_id
        AND server_id = v_server_id;

    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id = v_hook_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterServerHookStatus (
    v_hook_id UUID,
    v_server_id UUID,
    v_hook_status VARCHAR(100)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_server_hooks
    SET hook_status = v_hook_status,
        _update_date = LOCALTIMESTAMP
    WHERE hook_id = v_hook_id
        AND server_id = v_server_id;

    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id = v_hook_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateGlusterServerHookChecksum (
    v_hook_id UUID,
    v_server_id UUID,
    v_checksum VARCHAR(100)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE gluster_server_hooks
    SET checksum = v_checksum,
        _update_date = LOCALTIMESTAMP
    WHERE hook_id = v_hook_id
        AND server_id = v_server_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterServerHookById (v_hook_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_server_hooks
    WHERE hook_id = v_hook_id;

    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id = v_hook_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterServerHooksByIds (v_ids TEXT)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_server_hooks
    WHERE hook_id IN (
            SELECT *
            FROM fnSplitterUuid(v_ids)
            );

    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id IN (
            SELECT *
            FROM fnSplitterUuid(v_ids)
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteGlusterServerHook (
    v_hook_id UUID,
    v_server_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM gluster_server_hooks
    WHERE hook_id = v_hook_id
        AND server_id = v_server_id;

    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id = v_hook_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



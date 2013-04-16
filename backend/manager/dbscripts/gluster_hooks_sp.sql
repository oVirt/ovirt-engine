/* ----------------------------------------------------------------
 Stored procedures for database operations on Gluster Hooks
 related tables:
      - gluster_hooks
      - gluster_server_hooks
----------------------------------------------------------------*/

Create or replace FUNCTION InsertGlusterHook(v_id UUID,
                                             v_cluster_id UUID,
                                             v_gluster_command VARCHAR(128),
                                             v_stage VARCHAR(50),
                                             v_name VARCHAR(256),
                                             v_hook_status VARCHAR(50),
                                             v_content_type VARCHAR(50),
                                             v_checksum VARCHAR(256),
                                             v_content TEXT,
                                             v_conflict_status INTEGER)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO gluster_hooks(id, cluster_id, gluster_command, stage, name, hook_status,
    content_type, checksum, content, conflict_status)
    VALUES (v_id, v_cluster_id, v_gluster_command, v_stage, v_name,v_hook_status,
    v_content_type, v_checksum, v_content, v_conflict_status);
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetGlusterHookById(v_id UUID,
                                              v_includeContent BOOLEAN=false)
RETURNS SETOF gluster_hooks
AS $procedure$
BEGIN
    RETURN QUERY SELECT id, cluster_id, gluster_command, stage, name,
    hook_status, content_type, checksum,
    CASE v_includeContent WHEN true THEN content
                          ELSE null::text
    END as content,
    conflict_status,
    _create_date, _update_date
    FROM  gluster_hooks
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterHooksByClusterId(v_cluster_id UUID)
RETURNS SETOF gluster_hooks
AS $procedure$
BEGIN
    RETURN QUERY SELECT id, cluster_id, gluster_command, stage, name,
    hook_status, content_type, checksum, null::text as content, conflict_status,
    _create_date, _update_date
    FROM  gluster_hooks
    WHERE cluster_id = v_cluster_id order by gluster_command asc, stage asc;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterServerHooksById(v_id UUID)
RETURNS SETOF gluster_server_hooks
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_server_hooks
    WHERE hook_id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterHook(v_cluster_id UUID,
                                          v_gluster_command VARCHAR(1000),
                                          v_stage VARCHAR(100),
                                          v_name VARCHAR(1000),
                                          v_includeContent BOOLEAN=false)
RETURNS SETOF gluster_hooks
AS $procedure$
BEGIN
    RETURN QUERY SELECT id, cluster_id, gluster_command, stage, name,
    hook_status, content_type, checksum,
    CASE v_includeContent WHEN true THEN content
                          ELSE null::text
    END as content,
    conflict_status,
    _create_date, _update_date
    FROM  gluster_hooks
    WHERE cluster_id = v_cluster_id AND gluster_command = v_gluster_command
    AND stage = v_stage AND name = v_name;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterHookById(v_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM gluster_hooks
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterHook(v_cluster_id UUID,
                                             v_gluster_command VARCHAR(1000),
                                             v_stage VARCHAR(100),
                                             v_name VARCHAR(1000))
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM gluster_hooks
    WHERE cluster_id = v_cluster_id AND gluster_command = v_gluster_command
    AND stage = v_stage AND name = v_name;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterHooksByIds(v_ids TEXT)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM gluster_hooks
    WHERE id in (select * from fnSplitterUuid(v_ids));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterHookConflictStatus(v_id UUID,
                                                           v_conflict_status INTEGER)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_hooks
    SET conflict_status = v_conflict_status,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateGlusterHookContentType(v_id UUID,
                                                        v_content_type VARCHAR(100))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_hooks
    SET content_type = v_content_type,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateGlusterHookContent(v_id UUID,
                                                    v_checksum VARCHAR(256),
                                                    v_content text)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_hooks
    SET checksum = v_checksum,
        content = v_content,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterHook(v_id UUID,
                                             v_hook_status VARCHAR(50),
                                             v_content_type VARCHAR(50),
                                             v_checksum VARCHAR(256),
                                             v_content TEXT,
                                             v_conflict_status INTEGER)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_hooks
    SET hook_status = v_hook_status,
        content_type = v_content_type,
        checksum = v_checksum,
        content = v_content,
        conflict_status = v_conflict_status,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION UpdateGlusterHookStatus(v_id UUID,
                                                   v_hook_status VARCHAR(50))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_hooks
    SET hook_status = v_hook_status,
        _update_date = LOCALTIMESTAMP
    WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertGlusterServerHook(v_hook_id UUID,
                                                   v_server_id UUID,
                                                   v_hook_status VARCHAR(50),
                                                   v_content_type VARCHAR(50),
                                                   v_checksum VARCHAR(256))
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO gluster_server_hooks(hook_id, server_id, hook_status, content_type, checksum)
    VALUES(v_hook_id, v_server_id, v_hook_status, v_content_type, v_checksum);
    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id = v_hook_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetGlusterServerHook(v_hook_id UUID,
                                                v_server_id UUID)
RETURNS SETOF gluster_server_hooks
AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM  gluster_server_hooks
    WHERE hook_id = v_hook_id AND server_id = v_server_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterServerHook(v_hook_id UUID,
                                                   v_server_id UUID,
                                                   v_hook_status VARCHAR(50),
                                                   v_content_type VARCHAR(50),
                                                   v_checksum VARCHAR(256))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_server_hooks
    SET hook_status = v_hook_status,
        content_type = v_content_type,
        checksum = v_checksum,
        _update_date = LOCALTIMESTAMP
    WHERE hook_id = v_hook_id AND server_id = v_server_id;
    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id = v_hook_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterServerHookStatus(v_hook_id UUID,
                                                         v_server_id UUID,
                                                         v_hook_status VARCHAR(100))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_server_hooks
    SET hook_status = v_hook_status,
        _update_date = LOCALTIMESTAMP
    WHERE hook_id = v_hook_id AND server_id = v_server_id;
    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id = v_hook_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateGlusterServerHookChecksum(v_hook_id UUID,
                                                           v_server_id UUID,
                                                           v_checksum VARCHAR(100))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE gluster_server_hooks
    SET checksum = v_checksum,
        _update_date = LOCALTIMESTAMP
    WHERE hook_id = v_hook_id AND server_id = v_server_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterServerHookById(v_hook_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM gluster_server_hooks
    WHERE hook_id = v_hook_id;
    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id = v_hook_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteGlusterServerHooksByIds(v_ids TEXT)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM gluster_server_hooks
    WHERE hook_id in (select * from fnSplitterUuid(v_ids));
    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id in (select * from fnSplitterUuid(v_ids));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteGlusterServerHook(v_hook_id UUID,
                                                   v_server_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM gluster_server_hooks
    WHERE hook_id = v_hook_id AND server_id = v_server_id;
    UPDATE gluster_hooks
    SET _update_date = LOCALTIMESTAMP
    WHERE id = v_hook_id;
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [async_tasks] Table
--
CREATE OR REPLACE FUNCTION Insertasync_tasks (
    v_action_type INT,
    v_result INT,
    v_status INT,
    v_user_id UUID,
    v_vdsm_task_id UUID,
    v_task_id UUID,
    v_step_id UUID,
    v_command_id UUID,
    v_root_command_id UUID,
    v_started_at TIMESTAMP WITH TIME ZONE,
    v_storage_pool_id UUID,
    v_async_task_type INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO async_tasks (
        action_type,
        result,
        status,
        user_id,
        vdsm_task_id,
        task_id,
        step_id,
        command_id,
        root_command_id,
        started_at,
        storage_pool_id,
        task_type
        )
    VALUES (
        v_action_type,
        v_result,
        v_status,
        v_user_id,
        v_vdsm_task_id,
        v_task_id,
        v_step_id,
        v_command_id,
        v_root_command_id,
        v_started_at,
        v_storage_pool_id,
        v_async_task_type
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Updateasync_tasks (
    v_action_type INT,
    v_result INT,
    v_status INT,
    v_user_id UUID,
    v_vdsm_task_id UUID,
    v_task_id UUID,
    v_step_id UUID,
    v_command_id UUID,
    v_root_command_id UUID,
    v_storage_pool_id UUID
    )
RETURNS VOID
    --The [async_tasks] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE async_tasks
    SET action_type = v_action_type,
        result = v_result,
        status = v_status,
        step_id = v_step_id,
        command_id = v_command_id,
        root_command_id = v_root_command_id,
        vdsm_task_id = v_vdsm_task_id,
        storage_pool_id = v_storage_pool_id
    WHERE task_id = v_task_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertOrUpdateAsyncTasks (
    v_action_type INT,
    v_result INT,
    v_status INT,
    v_user_id UUID,
    v_vdsm_task_id UUID,
    v_task_id UUID,
    v_step_id UUID,
    v_command_id UUID,
    v_root_command_id UUID,
    v_started_at TIMESTAMP WITH TIME ZONE,
    v_storage_pool_id UUID,
    v_async_task_type INT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    IF NOT EXISTS (
            SELECT 1
            FROM async_tasks
            WHERE async_tasks.task_id = v_task_id
            ) THEN
                  PERFORM Insertasync_tasks(
                      v_action_type,
                      v_result,
                      v_status,
                      v_user_id,
                      v_vdsm_task_id,
                      v_task_id,
                      v_step_id,
                      v_command_id,
                      v_root_command_id,
                      v_started_at,
                      v_storage_pool_id,
                      v_async_task_type);
             ELSE
                 PERFORM Updateasync_tasks(
                     v_action_type,
                     v_result,
                     v_status,
                     v_user_id,
                     v_vdsm_task_id,
                     v_task_id,
                     v_step_id,
                     v_command_id,
                     v_root_command_id,
                     v_storage_pool_id);
END

IF ;END;$PROCEDURE$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION InsertAsyncTaskEntities (
    v_task_id UUID,
    v_entity_id UUID,
    v_entity_type VARCHAR(128)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    IF NOT EXISTS (
            SELECT 1
            FROM async_tasks_entities
            WHERE async_task_id = v_task_id
                AND entity_id = v_entity_id
            ) THEN
        INSERT INTO async_tasks_entities (
            async_task_id,
            entity_id,
            entity_type
            )
        VALUES (
            v_task_id,
            v_entity_id,
            v_entity_type
            );
END

IF ;END;$PROCEDURE$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAsyncTasksIdsByEntityId (v_entity_id UUID)
RETURNS SETOF idUuidType STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT async_task_id
    FROM async_tasks_entities
    WHERE entity_id = v_entity_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAsyncTaskEntitiesByTaskId (v_task_id UUID)
RETURNS SETOF async_tasks_entities STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM async_tasks_entities
    WHERE async_task_id = v_task_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deleteasync_tasks (v_task_id UUID)
RETURNS INT AS $PROCEDURE$
DECLARE deleted_rows INT;

BEGIN
    DELETE
    FROM async_tasks
    WHERE task_id = v_task_id;

    GET DIAGNOSTICS deleted_rows = ROW_COUNT;

    RETURN deleted_rows;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAsyncTasksByVdsmTaskId (v_vdsm_task_id UUID)
RETURNS INT AS $PROCEDURE$
DECLARE deleted_rows INT;

BEGIN
    DELETE
    FROM async_tasks
    WHERE vdsm_task_id = v_vdsm_task_id;

    GET DIAGNOSTICS deleted_rows = ROW_COUNT;

    RETURN deleted_rows;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAsyncTasksByStoragePoolId (v_storage_pool_id UUID)
RETURNS SETOF async_tasks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM async_tasks
    WHERE storage_pool_id = v_storage_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromasync_tasks ()
RETURNS SETOF async_tasks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM async_tasks;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getasync_tasksBytask_id (v_task_id UUID)
RETURNS SETOF async_tasks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM async_tasks
    WHERE task_id = v_task_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAsyncTasksByEntityId (v_entity_id UUID)
RETURNS SETOF async_tasks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT async_tasks.*
    FROM async_tasks
    INNER JOIN async_tasks_entities
        ON async_task_id = task_id
    WHERE entity_id = v_entity_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAsyncTasksByVdsmTaskId (v_vdsm_task_id UUID)
RETURNS SETOF async_tasks STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM async_tasks
    WHERE vdsm_task_id = v_vdsm_task_id;
END;$PROCEDURE$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [async_tasks] Table
--
Create or replace FUNCTION Insertasync_tasks(v_action_type INTEGER,
	v_result INTEGER,
	v_status INTEGER,
	v_user_id UUID,
	v_vdsm_task_id UUID,
	v_task_id UUID,
	v_step_id UUID,
	v_command_id UUID,
	v_root_command_id UUID,
        v_started_at timestamp WITH TIME ZONE,
	v_storage_pool_id UUID,
	v_async_task_type INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO async_tasks(action_type, result, status, user_id, vdsm_task_id, task_id, step_id, command_id, root_command_id, started_at,storage_pool_id, task_type)
	VALUES(v_action_type, v_result, v_status, v_user_id, v_vdsm_task_id, v_task_id, v_step_id, v_command_id, v_root_command_id, v_started_at, v_storage_pool_id, v_async_task_type);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Updateasync_tasks(v_action_type INTEGER,
	v_result INTEGER,
	v_status INTEGER,
	v_user_id UUID,
	v_vdsm_task_id UUID,
	v_task_id UUID,
	v_step_id UUID,
	v_command_id UUID,
	v_root_command_id UUID,
        v_storage_pool_id UUID)
RETURNS VOID

	--The [async_tasks] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
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
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION InsertOrUpdateAsyncTasks(v_action_type INTEGER,
	v_result INTEGER,
	v_status INTEGER,
	v_user_id UUID,
	v_vdsm_task_id UUID,
	v_task_id UUID,
	v_step_id UUID,
	v_command_id UUID,
	v_root_command_id UUID,
        v_started_at timestamp WITH TIME ZONE,
	v_storage_pool_id UUID,
	v_async_task_type INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      IF NOT EXISTS (SELECT 1 from async_tasks where async_tasks.task_id = v_task_id) THEN
            PERFORM Insertasync_tasks(v_action_type, v_result, v_status, v_user_id, v_vdsm_task_id, v_task_id,
            v_step_id, v_command_id, v_root_command_id, v_started_at, v_storage_pool_id, v_async_task_type);
      ELSE
            PERFORM Updateasync_tasks(v_action_type, v_result, v_status, v_user_id, v_vdsm_task_id, v_task_id, v_step_id, v_command_id, v_root_command_id, v_storage_pool_id);
      END IF;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertAsyncTaskEntities(
        v_task_id UUID,
        v_entity_id UUID,
        v_entity_type varchar(128))

RETURNS VOID
   AS $procedure$
BEGIN
      IF NOT EXISTS (SELECT 1 from async_tasks_entities where async_task_id = v_task_id and entity_id = v_entity_id) THEN
            INSERT INTO async_tasks_entities (async_task_id,entity_id,entity_type) VALUES (v_task_id, v_entity_id, v_entity_type);
      END IF;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAsyncTasksIdsByEntityId(v_entity_id UUID)
RETURNS SETOF idUuidType STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT async_task_id from async_tasks_entities where entity_id = v_entity_id;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAsyncTasksIdsByUserId(v_user_id UUID)
RETURNS SETOF idUuidType STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT task_id from async_tasks where user_id = v_user_id;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetVdsmTasksIdsByUserId(v_user_id UUID)
RETURNS SETOF idUuidType STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT vdsm_task_id from async_tasks where user_id = v_user_id;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION  GetAsyncTaskEntitiesByTaskId(v_task_id UUID)
RETURNS SETOF async_tasks_entities STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM async_tasks_entities
   WHERE async_task_id = v_task_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Deleteasync_tasks(v_task_id UUID)
RETURNS integer
   AS $procedure$
DECLARE
deleted_rows int;
BEGIN
   DELETE FROM async_tasks
   WHERE task_id = v_task_id;
   GET DIAGNOSTICS deleted_rows = ROW_COUNT;
   RETURN deleted_rows;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteAsyncTasksByVdsmTaskId(v_vdsm_task_id UUID)
RETURNS integer
   AS $procedure$
DECLARE
deleted_rows int;
BEGIN
   DELETE FROM async_tasks
   WHERE vdsm_task_id = v_vdsm_task_id;
   GET DIAGNOSTICS deleted_rows = ROW_COUNT;
   RETURN deleted_rows;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAsyncTasksByStoragePoolId(v_storage_pool_id UUID)
RETURNS SETOF idUuidType STABLE
  AS $procedure$
BEGIN
   RETURN QUERY SELECT async_tasks.task_id
   FROM  async_tasks
   WHERE storage_pool_id = v_storage_pool_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromasync_tasks() RETURNS SETOF async_tasks STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM async_tasks;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getasync_tasksBytask_id(v_task_id UUID) RETURNS SETOF async_tasks STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM async_tasks
   WHERE task_id = v_task_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAsyncTasksByEntityId(v_entity_id UUID) RETURNS SETOF async_tasks STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT async_tasks.*
   FROM async_tasks
   JOIN async_tasks_entities ON async_task_id = task_id
   WHERE entity_id = v_entity_id;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAsyncTasksByVdsmTaskId(v_vdsm_task_id UUID) RETURNS SETOF async_tasks STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM async_tasks
   WHERE vdsm_task_id = v_vdsm_task_id;

END; $procedure$
LANGUAGE plpgsql;

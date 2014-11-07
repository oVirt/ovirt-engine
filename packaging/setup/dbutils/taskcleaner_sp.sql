/************************************************************************************************
 The following are helper SP for taskcleaner utility and are not exposed to the application DAOs
************************************************************************************************/
CREATE OR REPLACE FUNCTION GetAsyncTasksZombies() RETURNS SETOF async_tasks
   AS $procedure$
DECLARE
    zombie_task_life varchar;
    zombie_date date;
BEGIN
   zombie_task_life = option_value FROM vdc_options WHERE option_name = 'AsyncTaskZombieTaskLifeInMinutes';
   EXECUTE 'SELECT now() - interval ''' || zombie_task_life || ' minute'''  INTO zombie_date;

   RETURN QUERY SELECT *
   FROM async_tasks
   WHERE started_at < zombie_date
   ORDER BY command_id;

END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAsyncTaskZombiesByTaskId(v_task_id UUID) RETURNS VOID
   AS $procedure$
BEGIN
    IF EXISTS (SELECT 1 FROM GetAsyncTasksZombies() WHERE task_id = v_task_id) THEN
        PERFORM Deleteasync_tasks(v_task_id);
    END IF;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAsyncTaskZombiesByCommandId(v_command_id UUID) RETURNS VOID
   AS $procedure$
DECLARE
deleted_rows int;
BEGIN
    IF EXISTS (SELECT 1 FROM GetAsyncTasksZombies() WHERE command_id = v_command_id) THEN
        DELETE FROM async_tasks WHERE command_id = v_command_id;
    END IF;
    DELETE FROM command_entities where command_id = v_command_id;
    GET DIAGNOSTICS deleted_rows = ROW_COUNT;
    IF deleted_rows > 0 THEN
        DELETE FROM command_entities C WHERE command_id = root_command_id_of_deleted_cmds AND NOT EXISTS (SELECT * from COMMAND_ENTITIES WHERE root_command_id = C.command_id);
    END IF;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAsyncTaskByCommandId(v_command_id UUID) RETURNS VOID
   AS $procedure$
DECLARE
deleted_rows int;
root_command_id_of_deleted_cmds UUID;
BEGIN
        DELETE FROM async_tasks WHERE command_id = v_command_id;
        SELECT root_command_id  into root_command_id_of_deleted_cmds FROM COMMAND_entities WHERE command_id = v_command_id;
        DELETE FROM command_entities where command_id = v_command_id;
        GET DIAGNOSTICS deleted_rows = ROW_COUNT;
        IF deleted_rows > 0 THEN
                DELETE FROM command_entities C WHERE command_id = root_command_id_of_deleted_cmds AND NOT EXISTS (SELECT * from COMMAND_ENTITIES WHERE root_command_id = C.command_id);
        END IF;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAsyncTasksZombies() RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM async_tasks WHERE task_id in (SELECT task_id FROM GetAsyncTasksZombies());
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteJobStepsByTaskId(v_task_id UUID) RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM step WHERE external_id = v_task_id;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteEntitySnapshotByZombieTaskId(v_task_id UUID) RETURNS VOID
   AS $procedure$
DECLARE
    v_command_id UUID;
BEGIN
    v_command_id:=command_id FROM GetAsyncTasksZombies() WHERE task_id = v_task_id;
    DELETE FROM business_entity_snapshot WHERE command_id = v_command_id;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteEntitySnapshotByTaskId(v_task_id UUID) RETURNS VOID
   AS $procedure$
DECLARE
    v_command_id UUID;
BEGIN
    v_command_id:=command_id FROM async_tasks WHERE task_id = v_task_id;
    DELETE FROM business_entity_snapshot WHERE command_id = v_command_id;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteEntitySnapshotZombies() RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM business_entity_snapshot WHERE command_id IN (SELECT command_id FROM GetAsyncTasksZombies());
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteJobStepsByZombieCommandId(v_command_id UUID) RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM step WHERE step_id IN (SELECT step_id from GetAsyncTasksZombies() WHERE command_id = v_command_id);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteJobStepsByCommandId(v_command_id UUID) RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM step WHERE step_id IN (SELECT step_id from async_tasks WHERE command_id = v_command_id);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteJobStepsZombies() RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM step WHERE step_id IN (SELECT step_id FROM GetAsyncTasksZombies());
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllJobs() RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM job;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllEntitySnapshot() RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM business_entity_snapshot;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAllCommandsWithRunningTasks() RETURNS SETOF COMMAND_ENTITIES STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM COMMAND_ENTITIES C
   WHERE EXISTS (SELECT * FROM ASYNC_TASKS A WHERE A.COMMAND_ID = C.COMMAND_ID);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION  GetAllCommands()
RETURNS SETOF COMMAND_ENTITIES STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM COMMAND_ENTITIES;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteAllCommands()
RETURNS integer
   AS $procedure$
DECLARE
deleted_rows int;
BEGIN
   DELETE FROM COMMAND_ENTITIES;
   GET DIAGNOSTICS deleted_rows = ROW_COUNT;
   RETURN deleted_rows;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION DeleteAllCommandsWithRunningTasks()
RETURNS integer
   AS $procedure$
DECLARE
deleted_rows int;
BEGIN
   DELETE FROM COMMAND_ENTITIES C WHERE EXISTS (SELECT * FROM ASYNC_TASKS A WHERE A.COMMAND_ID = C.COMMAND_ID);
   GET DIAGNOSTICS deleted_rows = ROW_COUNT;
   RETURN deleted_rows;

END; $procedure$
LANGUAGE plpgsql;

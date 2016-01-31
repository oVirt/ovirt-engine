/************************************************************************************************
 The following are helper SP for taskcleaner utility and are not exposed to the application DAOs

If you add a function here, drop it in taskcleaner_sp_drop.sql
************************************************************************************************/
CREATE OR REPLACE FUNCTION GetAsyncTasksZombies() RETURNS SETOF async_tasks
   AS $procedure$
DECLARE
    zombie_task_life varchar;
    zombie_timestamptz timestamp with time zone;
BEGIN
   zombie_task_life = option_value FROM vdc_options WHERE option_name = 'AsyncTaskZombieTaskLifeInMinutes';
   EXECUTE 'SELECT now() - interval ''' || zombie_task_life || ' minute'''  INTO zombie_timestamptz;

   RETURN QUERY SELECT *
   FROM async_tasks
   WHERE started_at < zombie_timestamptz
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
root_command_id_of_deleted_cmds UUID;
BEGIN
    IF (fn_db_is_table_exists ('command_entities')) THEN
        DELETE FROM command_entities c WHERE
        c.command_id IN (
            SELECT command_id FROM GetAsyncTasksZombies() t WHERE t.command_id = v_command_id
        );

        DELETE FROM command_entities c WHERE
        c.command_id IN (
            SELECT root_command_id FROM GetAsyncTasksZombies() t WHERE t.root_command_id = v_command_id
        );
    END IF;

    DELETE FROM async_tasks WHERE
    root_command_id IN (
        SELECT root_command_id FROM GetAsyncTasksZombies() t WHERE t.root_command_id = v_command_id
    );

    DELETE FROM async_tasks WHERE
    root_command_id IN (
        SELECT root_command_id FROM GetAsyncTasksZombies() t WHERE t.command_id = v_command_id
    );

    PERFORM DeleteEntitySnapshotByCommandId(v_command_id);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAsyncTaskByCommandId(v_command_id UUID) RETURNS VOID
   AS $procedure$
DECLARE
deleted_rows int;
root_command_id_of_deleted_cmds UUID;
BEGIN
        DELETE FROM async_tasks WHERE command_id = v_command_id;
        IF (fn_db_is_table_exists ('command_entities')) THEN
            SELECT root_command_id  into root_command_id_of_deleted_cmds FROM COMMAND_entities WHERE command_id = v_command_id;
            DELETE FROM command_entities where command_id = v_command_id;
            GET DIAGNOSTICS deleted_rows = ROW_COUNT;
            IF deleted_rows > 0 THEN
                DELETE FROM command_entities C WHERE command_id = root_command_id_of_deleted_cmds AND NOT EXISTS (SELECT * from COMMAND_ENTITIES WHERE root_command_id = C.command_id);
            END IF;
        END IF;
        PERFORM DeleteEntitySnapshotByCommandId(v_command_id);
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
    PERFORM DeleteCommandEntitiesByCommandId(v_command_id);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteEntitySnapshotByTaskId(v_task_id UUID) RETURNS VOID
   AS $procedure$
DECLARE
    v_command_id UUID;
BEGIN
    v_command_id:=command_id FROM async_tasks WHERE task_id = v_task_id;
    DELETE FROM business_entity_snapshot WHERE command_id = v_command_id;
    PERFORM DeleteCommandEntitiesByCommandId(v_command_id);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteEntitySnapshotZombies() RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM business_entity_snapshot WHERE command_id IN (SELECT command_id FROM GetAsyncTasksZombies());
    PERFORM DeleteAllCommandEntitiesByCommandId(v_command_id);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteOrphanJobs() RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM job WHERE NOT EXISTS (SELECT 1 from step where step.job_id = job.job_id);
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION DeleteJobStepsByZombieCommandId(v_command_id UUID) RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM step WHERE step_id IN (SELECT step_id from GetAsyncTasksZombies() WHERE command_id = v_command_id);
    PERFORM DeleteOrphanJobs();
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteJobStepsByCommandId(v_command_id UUID) RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM step WHERE step_id IN (SELECT step_id from async_tasks WHERE command_id = v_command_id);
    PERFORM DeleteOrphanJobs();
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteJobStepsZombies() RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM step WHERE step_id IN (SELECT step_id FROM GetAsyncTasksZombies());
    PERFORM DeleteOrphanJobs();
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
    PERFORM DeleteAllCommandEntities();
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteEntitySnapshotByCommandId(v_command_id UUID) RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM business_entity_snapshot where command_id = v_command_id;
    PERFORM DeleteCommandEntitiesByCommandId(v_command_id);
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllCommandEntities() RETURNS VOID
   AS $procedure$
BEGIN
    IF (fn_db_is_table_exists ('command_entities')) THEN
        DELETE FROM command_entities;
    END IF;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteCommandEntitiesByCommandId(v_command_id UUID) RETURNS VOID
   AS $procedure$
BEGIN
    IF (fn_db_is_table_exists ('command_entities')) THEN
        DELETE FROM command_entities where command_id = v_command_id;
    END IF;
END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteAllCommandEntitiesByCommandId(v_command_id UUID) RETURNS VOID
   AS $procedure$
BEGIN
    IF (fn_db_is_table_exists ('command_entities')) THEN
        DELETE FROM command_entities where command_id IN (SELECT command_id FROM GetAsyncTasksZombies());
    END IF;
END; $procedure$
LANGUAGE plpgsql;

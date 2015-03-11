------------------------------------
-- Inserts Job entity into Job table
------------------------------------
Create or replace FUNCTION InsertJob(
    v_job_id UUID,
    v_action_type VARCHAR(50),
    v_description TEXT,
    v_status VARCHAR(32),
    v_owner_id UUID,
    v_visible BOOLEAN,
    v_start_time TIMESTAMP WITH TIME ZONE,
    v_end_time TIMESTAMP WITH TIME ZONE,
    v_last_update_time TIMESTAMP WITH TIME ZONE,
    v_correlation_id VARCHAR(50),
    v_is_external boolean,
    v_is_auto_cleared boolean)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO job(
        job_id,
        action_type,
        description,
        status,
        owner_id,
        visible,
        start_time,
        end_time,
        last_update_time,
        correlation_id,
        is_external,
        is_auto_cleared)
    VALUES (
        v_job_id,
        v_action_type,
        v_description,
        v_status,
        v_owner_id,
        v_visible,
        v_start_time,
        v_end_time,
        v_last_update_time,
        v_correlation_id,
        v_is_external,
        v_is_auto_cleared);
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------
-- Retrieves Job entity by its job-id from Job table
----------------------------------------------------
Create or replace FUNCTION GetJobByJobId(v_job_id UUID)
RETURNS SETOF job STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT job.*
    FROM JOB
    WHERE job_id = v_job_id;
END; $procedure$
LANGUAGE plpgsql;

--------------------------------------------
-- Retrieves All Job entitise from Job table
--------------------------------------------
Create or replace FUNCTION GetAllJobs()
RETURNS SETOF job STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT job.*
    FROM JOB
    where status != 'UNKNOWN'
    order by start_time DESC;
END; $procedure$
LANGUAGE plpgsql;

-----------------------------------------------------
-- Retrieves All Job entities by offset and page size
-----------------------------------------------------
Create or replace FUNCTION GetJobsByOffsetAndPageSize(v_position INTEGER, v_page_size INTEGER)
RETURNS SETOF job STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    (SELECT job.*
    FROM JOB
    WHERE status = 'STARTED'
    ORDER BY last_update_time desc)
    UNION ALL
    (SELECT job.*
    FROM JOB
    WHERE status not in ('STARTED','UNKNOWN')
    ORDER BY last_update_time desc)
    OFFSET v_position LIMIT v_page_size;
END; $procedure$
LANGUAGE plpgsql;

-----------------------------------------------
-- Retrieves All Job entities by Correlation-ID
-----------------------------------------------
Create or replace FUNCTION GetJobsByCorrelationId(v_correlation_id VARCHAR(50))
RETURNS SETOF job STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT job.*
    FROM JOB
    WHERE correlation_id = v_correlation_id;
END; $procedure$
LANGUAGE plpgsql;

----------------------------------
-- Updates Job entity in Job table
----------------------------------
Create or replace FUNCTION UpdateJob(
    v_job_id UUID,
    v_action_type VARCHAR(50),
    v_description TEXT,
    v_status VARCHAR(32),
    v_owner_id UUID,
    v_visible BOOLEAN,
    v_start_time TIMESTAMP WITH TIME ZONE,
    v_end_time TIMESTAMP WITH TIME ZONE,
    v_last_update_time TIMESTAMP WITH TIME ZONE,
    v_correlation_id VARCHAR(50),
    v_is_external boolean,
    v_is_auto_cleared boolean)
RETURNS VOID
AS $procedure$
BEGIN
    update job
    SET action_type = v_action_type,
        description = v_description,
        status = v_status,
        owner_id = v_owner_id,
        visible = v_visible,
        start_time = v_start_time,
        end_time = v_end_time,
        last_update_time = v_last_update_time,
        correlation_id = v_correlation_id,
        is_external = v_is_external,
        is_auto_cleared = v_is_auto_cleared
    WHERE job_id = v_job_id;
END; $procedure$
LANGUAGE plpgsql;

-------------------------------------------------------
-- Updates Job entity for last update time in Job table
-------------------------------------------------------
Create or replace FUNCTION UpdateJobLastUpdateTime(
    v_job_id UUID,
    v_last_update_time TIMESTAMP WITH TIME ZONE)
RETURNS VOID
AS $procedure$
BEGIN
    update job
    SET last_update_time = v_last_update_time
    WHERE job_id = v_job_id;
END; $procedure$
LANGUAGE plpgsql;

--------------------------------------------
-- Deletes Job entity by status and end time
--------------------------------------------
Create or replace FUNCTION DeleteJobOlderThanDateWithStatus(
    v_end_time TIMESTAMP WITH TIME ZONE,
    v_status TEXT)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM job
    WHERE is_auto_cleared
    AND end_time < v_end_time
    AND status = any (string_to_array(v_status,',')::VARCHAR[] );
END; $procedure$
LANGUAGE plpgsql;

-------------------------------
-- Deletes Job entity by Job-Id
-------------------------------
Create or replace FUNCTION DeleteJob(
    v_job_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM job
    WHERE job_id = v_job_id;
END; $procedure$
LANGUAGE plpgsql;

---------------------------------------------------------------
-- Inserts Job Subject Entity Into job_subject_entity table
---------------------------------------------------------------
Create or replace FUNCTION InsertJobSubjectEntity(
    v_job_id UUID,
    v_entity_id UUID,
    v_entity_type VARCHAR(32))
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO job_subject_entity(
        job_id,
        entity_id,
        entity_type)
    VALUES (
        v_job_id,
        v_entity_id,
        v_entity_type);
END; $procedure$
LANGUAGE plpgsql;


---------------------------------------------
-- Gets Job Subject Entity of a Job By Job-id
---------------------------------------------
Create or replace FUNCTION GetJobSubjectEntityByJobId(v_job_id UUID)
RETURNS SETOF job_subject_entity STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT job_subject_entity.*
    FROM job_subject_entity
    WHERE job_id = v_job_id;
END; $procedure$
LANGUAGE plpgsql;

------------------------------------------------
-- Gets Job Subject Entity of a Job By entity-id
------------------------------------------------
Create or replace FUNCTION GetAllJobIdsByEntityId(
    v_entity_id UUID)
RETURNS SETOF UUID STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT job_subject_entity.job_id
    FROM job_subject_entity
    WHERE entity_id = v_entity_id;
END; $procedure$
LANGUAGE plpgsql;

--------------------------------------
-- Inserts Step entity into Step table
--------------------------------------
Create or replace FUNCTION InsertStep(
    v_step_id UUID,
    v_parent_step_id UUID,
    v_job_id UUID,
    v_step_type VARCHAR(32),
    v_description TEXT,
    v_step_number INTEGER,
    v_status VARCHAR(32),
    v_start_time TIMESTAMP WITH TIME ZONE,
    v_end_time TIMESTAMP WITH TIME ZONE,
    v_correlation_id VARCHAR(50),
    v_external_id UUID,
    v_external_system_type VARCHAR(32),
    v_is_external boolean)
RETURNS VOID
AS $procedure$
BEGIN
    INSERT INTO step(
        step_id,
        parent_step_id,
        job_id,
        step_type,
        description,
        step_number,
        status,
        start_time,
        end_time,
        correlation_id,
        external_id,
        external_system_type,
        is_external)
    VALUES (
        v_step_id,
        v_parent_step_id,
        v_job_id,
        v_step_type,
        v_description,
        v_step_number,
        v_status,
        v_start_time,
        v_end_time,
        v_correlation_id,
        v_external_id,
        v_external_system_type,
        v_is_external);
END; $procedure$
LANGUAGE plpgsql;

------------------------------------
-- Updates Step entity in Step table
------------------------------------
Create or replace FUNCTION UpdateStep(
    v_step_id UUID,
    v_parent_step_id UUID,
    v_job_id UUID,
    v_step_type VARCHAR(32),
    v_description TEXT,
    v_step_number INTEGER,
    v_status VARCHAR(32),
    v_start_time TIMESTAMP WITH TIME ZONE,
    v_end_time TIMESTAMP WITH TIME ZONE,
    v_correlation_id VARCHAR(50),
    v_external_id UUID,
    v_external_system_type VARCHAR(32))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE step
    SET parent_step_id = v_parent_step_id,
        job_id = v_job_id,
        step_type = v_step_type,
        description = v_description,
        step_number = v_step_number,
        status = v_status,
        start_time = v_start_time,
        end_time = v_end_time,
        correlation_id = v_correlation_id,
        external_id = v_external_id,
        external_system_type = v_external_system_type
    WHERE step_id = v_step_id;
END; $procedure$
LANGUAGE plpgsql;

--------------------------------------
-- Updates Step entity status and time
--------------------------------------
Create or replace FUNCTION UpdateStepStatusAndEndTime(
    v_step_id UUID,
    v_status VARCHAR(32),
    v_end_time TIMESTAMP WITH TIME ZONE)
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE step
    SET status = v_status,
        end_time = v_end_time
    WHERE step_id = v_step_id;
END; $procedure$
LANGUAGE plpgsql;

------------------------------------------------------
-- Updates Step entity external id and external system
------------------------------------------------------
Create or replace FUNCTION UpdateStepExternalIdAndType(
    v_step_id UUID,
    v_external_id UUID,
    v_external_system_type VARCHAR(32))
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE step
    SET external_id = v_external_id,
        external_system_type = v_external_system_type
    WHERE step_id = v_step_id;
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------
-- Gets Step entity from Step table by Step-Id
----------------------------------------------
Create or replace FUNCTION GetStepByStepId(v_step_id UUID)
RETURNS SETOF step STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT step.*
    FROM step
    WHERE step_id = v_step_id;
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------
-- Gets Step entities list from Step table by Job-Id
----------------------------------------------------
Create or replace FUNCTION GetStepsByJobId(v_job_id UUID)
RETURNS SETOF step STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT step.*
    FROM step
    WHERE job_id = v_job_id
    ORDER BY parent_step_id nulls first, step_number;
END; $procedure$
LANGUAGE plpgsql;

------------------------------------------------------------
-- Gets Step entities list from Step table by parent-step-id
------------------------------------------------------------
Create or replace FUNCTION GetStepsByParentStepId(v_parent_step_id UUID)
RETURNS SETOF step STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT step.*
    FROM step
    WHERE parent_step_id = v_parent_step_id
    ORDER BY step_number;
END; $procedure$
LANGUAGE plpgsql;

---------------------------------------------
-- Gets Step entity from Step table by Job-Id
---------------------------------------------
Create or replace FUNCTION GetAllSteps()
RETURNS SETOF step STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT step.*
    FROM step;
END; $procedure$
LANGUAGE plpgsql;

---------------------------------
-- Deletes Step entity by Step-Id
---------------------------------
Create or replace FUNCTION DeleteStep(
    v_step_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM step
    WHERE step_id = v_step_id;
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------
-- Updates steps related to a Job as completed
----------------------------------------------
Create or replace FUNCTION updateJobStepsCompleted(
    v_job_id UUID,
    v_status VARCHAR(32),
    v_end_time TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID
AS $procedure$
BEGIN
    UPDATE step
    SET status = v_status,
        end_time = v_end_time
    WHERE job_id = v_job_id
    AND   status = 'STARTED'
    AND   STATUS != v_status;
END; $procedure$
LANGUAGE plpgsql;


-------------------------------------------
-- Updates Job and Step statuses to UNKNOWN
-------------------------------------------
Create or replace FUNCTION UpdateStartedExecutionEntitiesToUnknown(
    v_end_time TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID
AS $procedure$
BEGIN

update job
set status = 'UNKNOWN',
    end_time = v_end_time,
    last_update_time = v_end_time
where job.status = 'STARTED'
and job_id not in (select job_id
                   from step
                   where external_id is not null);

update step
set status = 'UNKNOWN',
    end_time = v_end_time
where status = 'STARTED'
and job_id not in (select step.job_id
                   from step step
                   where step.external_id is not null);

END; $procedure$
LANGUAGE plpgsql;

------------------------------------------------
-- Cleanup Jobs of async commands without task
------------------------------------------------
Create or replace FUNCTION DeleteRunningJobsOfTasklessCommands()
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM job
    WHERE (status = 'STARTED'
    AND    action_type IN ('MigrateVm', 'MigrateVmToServer', 'RunVm', 'RunVmOnce'))
    AND    job_id not in (select job_id from step where step_id in (select step_id from async_tasks));

    DELETE FROM job
    WHERE job_id IN
          (SELECT job_id from step WHERE (status = 'STARTED' AND step_type='MIGRATE_VM'));
END; $procedure$
LANGUAGE plpgsql;

--------------------------------------------
-- Cleanup Jobs entities by end time
--------------------------------------------
Create or replace FUNCTION DeleteCompletedJobsOlderThanDate(
    v_succeeded_end_time TIMESTAMP WITH TIME ZONE,
    v_failed_end_time TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID
AS $procedure$
BEGIN
    DELETE FROM job
    WHERE (is_auto_cleared
    AND ((end_time < v_succeeded_end_time
    AND    status = 'FINISHED')
    OR    (end_time < v_failed_end_time
    AND    status IN ('FAILED', 'ABORTED', 'UNKNOWN'))));
END; $procedure$
LANGUAGE plpgsql;

-------------------------------------
-- Checks if a Job has step for tasks
-------------------------------------
Create or replace FUNCTION CheckIfJobHasTasks(
    v_job_id UUID)
RETURNS SETOF booleanResultType STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT EXISTS(
        SELECT *
        FROM   step
        WHERE  job_id = v_job_id
        AND    external_id is not null
        AND    external_system_type in ('VDSM','GLUSTER'));
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------
-- Gets Step entities list from Step table by external id
----------------------------------------------------
Create or replace FUNCTION GetStepsByExternalTaskId(v_external_id UUID)
RETURNS SETOF step STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT step.*
    FROM step
    WHERE external_id = v_external_id
    ORDER BY parent_step_id nulls first, step_number;
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------
-- Gets list of external task UUIDs from Step table
-- for steps based on external system type and job status
----------------------------------------------------
Create or replace FUNCTION GetExternalIdsFromSteps(v_status VARCHAR(32),
                                                   v_external_system_type VARCHAR(32))
RETURNS SETOF UUID STABLE
AS $procedure$
BEGIN
    RETURN QUERY SELECT step.external_id
    FROM step
    INNER JOIN job ON step.job_id = job.job_id
    WHERE job.status = v_status
    AND step.external_system_type = v_external_system_type;
END; $procedure$
LANGUAGE plpgsql;

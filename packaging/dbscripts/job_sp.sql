

------------------------------------
-- Inserts Job entity into Job table
------------------------------------
CREATE OR REPLACE FUNCTION InsertJob (
    v_job_id UUID,
    v_action_type VARCHAR(50),
    v_description TEXT,
    v_status VARCHAR(32),
    v_owner_id UUID,
    v_engine_session_seq_id BIGINT,
    v_visible BOOLEAN,
    v_start_time TIMESTAMP WITH TIME ZONE,
    v_end_time TIMESTAMP WITH TIME ZONE,
    v_last_update_time TIMESTAMP WITH TIME ZONE,
    v_correlation_id VARCHAR(50),
    v_is_external boolean,
    v_is_auto_cleared boolean
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO job (
        job_id,
        action_type,
        description,
        status,
        owner_id,
        engine_session_seq_id,
        visible,
        start_time,
        end_time,
        last_update_time,
        correlation_id,
        is_external,
        is_auto_cleared
        )
    VALUES (
        v_job_id,
        v_action_type,
        v_description,
        v_status,
        v_owner_id,
        v_engine_session_seq_id,
        v_visible,
        v_start_time,
        v_end_time,
        v_last_update_time,
        v_correlation_id,
        v_is_external,
        v_is_auto_cleared
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------
-- Retrieves Job entity by its job-id from Job table
----------------------------------------------------
CREATE OR REPLACE FUNCTION GetJobByJobId (v_job_id UUID)
RETURNS SETOF job STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT job.*
    FROM JOB
    WHERE job_id = v_job_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

--------------------------------------------
-- Retrieves All Job entitise from Job table
--------------------------------------------
CREATE OR REPLACE FUNCTION GetAllJobs ()
RETURNS SETOF job STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT job.*
    FROM JOB
    WHERE status != 'UNKNOWN'
    ORDER BY start_time DESC;
END;$PROCEDURE$
LANGUAGE plpgsql;

-----------------------------------------------------
-- Retrieves All Job entities by offset and page size
-----------------------------------------------------
CREATE OR REPLACE FUNCTION GetJobsByOffsetAndPageSize (
    v_position INT,
    v_page_size INT
    )
RETURNS SETOF job STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY(SELECT job.* FROM JOB WHERE status = 'STARTED' ORDER BY last_update_time DESC)

    UNION ALL

    (
        SELECT job.*
        FROM JOB
        WHERE status NOT IN (
                'STARTED',
                'UNKNOWN'
                )
        ORDER BY last_update_time DESC
        ) OFFSET v_position LIMIT v_page_size;
END;$PROCEDURE$
LANGUAGE plpgsql;

-----------------------------------------------
-- Retrieves All Job entities by Correlation-ID
-----------------------------------------------
CREATE OR REPLACE FUNCTION GetJobsByCorrelationId (v_correlation_id VARCHAR(50))
RETURNS SETOF job STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT job.*
    FROM JOB
    WHERE correlation_id = v_correlation_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-----------------------------------------------------------------
-- Retrieves All Job entities by Engine-Session-Seq-ID and Status
-----------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetJobsByEngineSessionSeqIdAndStatus (
    v_engine_session_seq_id BIGINT,
    v_status VARCHAR(32)
    )
RETURNS SETOF job STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT job.*
    FROM JOB
    WHERE engine_session_seq_id = v_engine_session_seq_id
        AND status = v_status;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------
-- Updates Job entity in Job table
----------------------------------
CREATE OR REPLACE FUNCTION UpdateJob (
    v_job_id UUID,
    v_action_type VARCHAR(50),
    v_description TEXT,
    v_status VARCHAR(32),
    v_owner_id UUID,
    v_engine_session_seq_id BIGINT,
    v_visible BOOLEAN,
    v_start_time TIMESTAMP WITH TIME ZONE,
    v_end_time TIMESTAMP WITH TIME ZONE,
    v_last_update_time TIMESTAMP WITH TIME ZONE,
    v_correlation_id VARCHAR(50),
    v_is_external boolean,
    v_is_auto_cleared boolean
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE job
    SET action_type = v_action_type,
        description = v_description,
        status = v_status,
        owner_id = v_owner_id,
        engine_session_seq_id = v_engine_session_seq_id,
        visible = v_visible,
        start_time = v_start_time,
        end_time = v_end_time,
        last_update_time = v_last_update_time,
        correlation_id = v_correlation_id,
        is_external = v_is_external,
        is_auto_cleared = v_is_auto_cleared
    WHERE job_id = v_job_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-------------------------------------------------------
-- Updates Job entity for last update time in Job table
-------------------------------------------------------
CREATE OR REPLACE FUNCTION UpdateJobLastUpdateTime (
    v_job_id UUID,
    v_last_update_time TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE job
    SET last_update_time = v_last_update_time
    WHERE job_id = v_job_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

--------------------------------------------
-- Deletes Job entity by status and end time
--------------------------------------------
CREATE OR REPLACE FUNCTION DeleteJobOlderThanDateWithStatus (
    v_end_time TIMESTAMP WITH TIME ZONE,
    v_status TEXT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM job
    WHERE is_auto_cleared
        AND end_time < v_end_time
        AND status = ANY (string_to_array(v_status, ',')::VARCHAR []);
END;$PROCEDURE$
LANGUAGE plpgsql;

-------------------------------
-- Deletes Job entity by Job-Id
-------------------------------
CREATE OR REPLACE FUNCTION DeleteJob (v_job_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM job
    WHERE job_id = v_job_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

---------------------------------------------------------------
-- Inserts Job Subject Entity Into job_subject_entity table
---------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertJobSubjectEntity (
    v_job_id UUID,
    v_entity_id UUID,
    v_entity_type VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO job_subject_entity (
        job_id,
        entity_id,
        entity_type
        )
    VALUES (
        v_job_id,
        v_entity_id,
        v_entity_type
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

---------------------------------------------
-- Gets Job Subject Entity of a Job By Job-id
---------------------------------------------
CREATE OR REPLACE FUNCTION GetJobSubjectEntityByJobId (v_job_id UUID)
RETURNS SETOF job_subject_entity STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT job_subject_entity.*
    FROM job_subject_entity
    WHERE job_id = v_job_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

------------------------------------------------
-- Gets Job Subject Entity of a Job By entity-id
------------------------------------------------
CREATE OR REPLACE FUNCTION GetAllJobIdsByEntityId (v_entity_id UUID)
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT job_subject_entity.job_id
    FROM job_subject_entity
    WHERE entity_id = v_entity_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

--------------------------------------
-- Inserts Step entity into Step table
--------------------------------------
CREATE OR REPLACE FUNCTION InsertStep (
    v_step_id UUID,
    v_parent_step_id UUID,
    v_job_id UUID,
    v_step_type VARCHAR(32),
    v_description TEXT,
    v_step_number INT,
    v_status VARCHAR(32),
    v_progress SMALLINT,
    v_start_time TIMESTAMP WITH TIME ZONE,
    v_end_time TIMESTAMP WITH TIME ZONE,
    v_correlation_id VARCHAR(50),
    v_external_id UUID,
    v_external_system_type VARCHAR(32),
    v_is_external boolean
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO step (
        step_id,
        parent_step_id,
        job_id,
        step_type,
        description,
        step_number,
        status,
        progress,
        start_time,
        end_time,
        correlation_id,
        external_id,
        external_system_type,
        is_external
        )
    VALUES (
        v_step_id,
        v_parent_step_id,
        v_job_id,
        v_step_type,
        v_description,
        v_step_number,
        v_status,
        v_progress,
        v_start_time,
        v_end_time,
        v_correlation_id,
        v_external_id,
        v_external_system_type,
        v_is_external
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

------------------------------------
-- Updates Step entity in Step table
------------------------------------
CREATE OR REPLACE FUNCTION UpdateStep (
    v_step_id UUID,
    v_parent_step_id UUID,
    v_job_id UUID,
    v_step_type VARCHAR(32),
    v_description TEXT,
    v_step_number INT,
    v_status VARCHAR(32),
    v_progress SMALLINT,
    v_start_time TIMESTAMP WITH TIME ZONE,
    v_end_time TIMESTAMP WITH TIME ZONE,
    v_correlation_id VARCHAR(50),
    v_external_id UUID,
    v_external_system_type VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE step
    SET parent_step_id = v_parent_step_id,
        job_id = v_job_id,
        step_type = v_step_type,
        description = v_description,
        step_number = v_step_number,
        status = v_status,
        progress = v_progress,
        start_time = v_start_time,
        end_time = v_end_time,
        correlation_id = v_correlation_id,
        external_id = v_external_id,
        external_system_type = v_external_system_type
    WHERE step_id = v_step_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

--------------------------------------
-- Updates Step entity status and time
--------------------------------------
CREATE OR REPLACE FUNCTION UpdateStepStatusAndEndTime (
    v_step_id UUID,
    v_status VARCHAR(32),
    v_end_time TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE step
    SET status = v_status,
        end_time = v_end_time
    WHERE step_id = v_step_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

------------------------------------------------------
-- Updates Step entity external id and external system
------------------------------------------------------
CREATE OR REPLACE FUNCTION UpdateStepExternalIdAndType (
    v_step_id UUID,
    v_external_id UUID,
    v_external_system_type VARCHAR(32)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE step
    SET external_id = v_external_id,
        external_system_type = v_external_system_type
    WHERE step_id = v_step_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------
-- Gets Step entity from Step table by Step-Id
----------------------------------------------
CREATE OR REPLACE FUNCTION GetStepByStepId (v_step_id UUID)
RETURNS SETOF step STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT step.*
    FROM step
    WHERE step_id = v_step_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------
-- Gets Step entities list from Step table by Job-Id
----------------------------------------------------
CREATE OR REPLACE FUNCTION GetStepsByJobId (v_job_id UUID)
RETURNS SETOF step STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT step.*
    FROM step
    WHERE job_id = v_job_id
    ORDER BY parent_step_id nulls first,
        step_number;
END;$PROCEDURE$
LANGUAGE plpgsql;

------------------------------------------------------------
-- Gets Step entities list from Step table by parent-step-id
------------------------------------------------------------
CREATE OR REPLACE FUNCTION GetStepsByParentStepId (v_parent_step_id UUID)
RETURNS SETOF step STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT step.*
    FROM step
    WHERE parent_step_id = v_parent_step_id
    ORDER BY step_number;
END;$PROCEDURE$
LANGUAGE plpgsql;

---------------------------------------------
-- Gets Step entity from Step table by Job-Id
---------------------------------------------
CREATE OR REPLACE FUNCTION GetAllSteps ()
RETURNS SETOF step STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT step.*
    FROM step;
END;$PROCEDURE$
LANGUAGE plpgsql;

---------------------------------
-- Deletes Step entity by Step-Id
---------------------------------
CREATE OR REPLACE FUNCTION DeleteStep (v_step_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM step
    WHERE step_id = v_step_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

---------------------------------------------------------------
-- Inserts StepSubjectEntity to the step_subject_entity table
---------------------------------------------------------------
CREATE OR REPLACE FUNCTION InsertStepSubjectEntity (
    v_step_id UUID,
    v_entity_id UUID,
    v_entity_type VARCHAR(32),
    v_step_entity_weight SMALLINT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO step_subject_entity (
        step_id,
        entity_id,
        entity_type,
        step_entity_weight
        )
    VALUES (
        v_step_id,
        v_entity_id,
        v_entity_type,
        v_step_entity_weight
        );
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION DeleteStepSubjectEntity (
    v_step_id UUID,
    v_entity_id UUID
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE FROM step_subject_entity sse
    WHERE sse.step_id = v_step_id
    AND sse.entity_id = v_entity_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-----------------------------------------------------
-- Get Step Subject Entities of a step by the step id
-----------------------------------------------------
CREATE OR REPLACE FUNCTION GetStepSubjectEntitiesByStepId (v_step_id UUID)
RETURNS SETOF step_subject_entity STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT *
    FROM step_subject_entity
    WHERE step_id = v_step_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------
-- Updates steps related to a Job as completed
----------------------------------------------
CREATE OR REPLACE FUNCTION updateJobStepsCompleted (
    v_job_id UUID,
    v_status VARCHAR(32),
    v_end_time TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE step
    SET status = v_status,
        end_time = v_end_time
    WHERE job_id = v_job_id
        AND status = 'STARTED'
        AND status != v_status;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------
-- Updates step progress
----------------------------------------------
CREATE OR REPLACE FUNCTION updateStepProgress (
    v_step_id UUID,
    v_progress SMALLINT
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE step
    SET progress = v_progress
    WHERE step_id = v_step_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

-------------------------------------------
-- Updates Job and Step statuses to UNKNOWN
-------------------------------------------
CREATE OR REPLACE FUNCTION UpdateStartedExecutionEntitiesToUnknown (v_end_time TIMESTAMP WITH TIME ZONE)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE job
    SET status = 'UNKNOWN',
        end_time = v_end_time,
        last_update_time = v_end_time
    WHERE job.status = 'STARTED'
        AND job_id NOT IN (
            SELECT job_id
            FROM step
            WHERE external_id IS NOT NULL
            );

    UPDATE step
    SET status = 'UNKNOWN',
        end_time = v_end_time
    WHERE status = 'STARTED'
        AND job_id NOT IN (
            SELECT step.job_id
            FROM step step
            WHERE step.external_id IS NOT NULL
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

------------------------------------------------
-- Cleanup Jobs of async commands without task
------------------------------------------------
CREATE OR REPLACE FUNCTION DeleteRunningJobsOfTasklessCommands ()
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM job
    WHERE (
            status = 'STARTED'
            AND action_type IN (
                'MigrateVm',
                'MigrateVmToServer',
                'RunVm',
                'RunVmOnce'
                )
            )
        AND job_id NOT IN (
            SELECT job_id
            FROM step
            WHERE step_id IN (
                    SELECT step_id
                    FROM async_tasks
                    )
            );

    DELETE
    FROM job
    WHERE job_id IN (
            SELECT job_id
            FROM step
            WHERE (
                    status = 'STARTED'
                    AND step_type = 'MIGRATE_VM'
                    )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

--------------------------------------------
-- Cleanup Jobs entities by end time
--------------------------------------------
CREATE OR REPLACE FUNCTION DeleteCompletedJobsOlderThanDate (
    v_succeeded_end_time TIMESTAMP WITH TIME ZONE,
    v_failed_end_time TIMESTAMP WITH TIME ZONE
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM job
    WHERE (
            is_auto_cleared
            AND (
                (
                    end_time < v_succeeded_end_time
                    AND status = 'FINISHED'
                    )
                OR (
                    end_time < v_failed_end_time
                    AND status IN (
                        'FAILED',
                        'ABORTED',
                        'UNKNOWN'
                        )
                    )
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

-------------------------------------
-- Checks if a Job has step for tasks
-------------------------------------
CREATE OR REPLACE FUNCTION CheckIfJobHasTasks (v_job_id UUID)
RETURNS SETOF booleanResultType STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT EXISTS (
            SELECT *
            FROM step
            WHERE job_id = v_job_id
                AND external_id IS NOT NULL
                AND external_system_type IN (
                    'VDSM',
                    'GLUSTER'
                    )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------
-- Gets Step entities list from Step table by external id
----------------------------------------------------
CREATE OR REPLACE FUNCTION GetStepsByExternalTaskId (v_external_id UUID)
RETURNS SETOF step STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT step.*
    FROM step
    WHERE external_id = v_external_id
    ORDER BY parent_step_id nulls first,
        step_number;
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------
-- Gets list of external task UUIDs from Step table
-- for steps based on external system type and job status
----------------------------------------------------
CREATE OR REPLACE FUNCTION GetExternalIdsFromSteps (
    v_status VARCHAR(32),
    v_external_system_type VARCHAR(32)
    )
RETURNS SETOF UUID STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT step.external_id
    FROM step
    INNER JOIN job
        ON step.job_id = job.job_id
    WHERE job.status = v_status
        AND step.external_system_type = v_external_system_type;
END;$PROCEDURE$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetStepsForEntityByStatus (
    v_status VARCHAR(32),
    v_entity_id UUID,
    v_entity_type VARCHAR(32)
    )
RETURNS SETOF step STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY
    SELECT s.*
    FROM step_subject_entity sse
    INNER JOIN step s
    ON sse.step_id = s.step_id
    WHERE sse.entity_id = v_entity_id
        AND sse.entity_type = v_entity_type
        AND s.status = v_status;
END;$PROCEDURE$
LANGUAGE plpgsql;




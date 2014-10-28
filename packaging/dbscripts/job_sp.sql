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

----------------------------------------------------------------
-- [async_tasks] Table
--


Create or replace FUNCTION Insertasync_tasks(v_action_type INTEGER,
	v_result INTEGER,
	v_status INTEGER,
	v_task_id UUID,
	v_action_parameters BYTEA)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO async_tasks(action_type, result, status, task_id, action_parameters)
	VALUES(v_action_type, v_result, v_status, v_task_id, v_action_parameters);
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION Updateasync_tasks(v_action_type INTEGER,
	v_result INTEGER,
	v_status INTEGER,
	v_task_id UUID,
	v_action_parameters BYTEA)
RETURNS VOID

	--The [async_tasks] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE async_tasks
      SET action_type = v_action_type,result = v_result,status = v_status,action_parameters = v_action_parameters
      WHERE task_id = v_task_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deleteasync_tasks(v_task_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
	
   DELETE FROM async_tasks
   WHERE task_id = v_task_id;
    
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromasync_tasks() RETURNS SETOF async_tasks
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM async_tasks;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getasync_tasksBytask_id(v_task_id UUID) RETURNS SETOF async_tasks
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM async_tasks
   WHERE task_id = v_task_id;

END; $procedure$
LANGUAGE plpgsql;






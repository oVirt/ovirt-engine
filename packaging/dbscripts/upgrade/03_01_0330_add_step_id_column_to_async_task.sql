-- Add column step_id to async_task table
select fn_db_add_column('async_tasks', 'step_id', 'UUID');


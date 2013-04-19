select fn_db_add_column('async_tasks', 'vdsm_task_id', 'UUID DEFAULT NULL');
CREATE INDEX IDX_vdsm_task_id ON async_tasks(vdsm_task_id);

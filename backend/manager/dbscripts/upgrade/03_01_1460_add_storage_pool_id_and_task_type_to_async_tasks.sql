select fn_db_add_column('async_tasks', 'storage_pool_id', 'uuid');
select fn_db_add_column('async_tasks', 'task_type','INTEGER NOT NULL DEFAULT 0');



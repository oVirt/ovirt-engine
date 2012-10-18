select fn_db_add_column('business_entity_snapshot', 'started_at', 'timestamp with time zone DEFAULT CURRENT_TIMESTAMP');
select fn_db_add_column('async_tasks', 'started_at', 'timestamp with time zone DEFAULT NULL');


SELECT fn_db_drop_column('command_entities', 'job_id');
SELECT fn_db_drop_column('command_entities', 'step_id');
SELECT fn_db_add_column('command_entities', 'command_context', 'TEXT');

SELECT fn_db_create_constraint('async_tasks', 'fk_async_tasks_command_entities_command_id', 'FOREIGN KEY (command_id) REFERENCES command_entities(command_id) ON DELETE CASCADE');
SELECT fn_db_create_constraint('async_tasks', 'fk_async_tasks_command_entities_root_command_id', 'FOREIGN KEY (root_command_id) REFERENCES command_entities(command_id) ON DELETE CASCADE');


select fn_db_rename_column('async_tasks', 'task_parameters', 'action_parameters');
select fn_db_rename_column('async_tasks', 'task_params_class', 'action_params_class');
select fn_db_rename_column('command_entities', 'action_parameters', 'command_parameters');
select fn_db_rename_column('command_entities', 'action_parameters_class', 'command_params_class');

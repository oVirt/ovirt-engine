select fn_db_add_column('async_tasks', 'root_command_id', 'UUID DEFAULT NULL');
select fn_db_drop_column ('async_tasks', 'action_parameters');
select fn_db_drop_column ('async_tasks', 'action_params_class');

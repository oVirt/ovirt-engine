-- Change action_parameters column from bytea (blob) to text (clob) in order to
--Keep the parameters in JSON format
select fn_db_change_column_type('async_tasks','action_parameters','bytea','text');
select fn_db_add_column('async_tasks', 'action_params_class', 'VARCHAR(256)');


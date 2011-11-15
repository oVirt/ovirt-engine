-- change var_datetime in table dwh_history_timekeeping to save time zone

select fn_db_change_column_type('dwh_history_timekeeping','var_datetime','timestamp without time zone','timestamp with time zone');

-- Inserting data to history timekeeping for host sync of slow changing data in dynamic configuration and statistical tables.

Insert into dwh_history_timekeeping  SELECT 'lastFullHostCheck',NULL,to_timestamp('01/01/2000', 'DD/MM/YYYY') WHERE not exists (SELECT var_name FROM dwh_history_timekeeping WHERE var_name ='lastFullHostCheck');


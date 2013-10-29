INSERT INTO dwh_history_timekeeping(var_name,var_value) SELECT 'DwhCurrentlyRunning','0' WHERE not exists (SELECT 1 FROM dwh_history_timekeeping WHERE var_name = 'DwhCurrentlyRunning');

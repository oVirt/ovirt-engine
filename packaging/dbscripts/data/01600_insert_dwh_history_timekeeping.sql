INSERT INTO dwh_history_timekeeping SELECT 'lastFullHostCheck',NULL,to_timestamp('01/01/2000', 'DD/MM/YYYY') WHERE not exists (SELECT 1 FROM dwh_history_timekeeping WHERE var_name ='lastFullHostCheck');

INSERT INTO dwh_history_timekeeping SELECT 'lastOsinfoUpdate',NULL,to_timestamp('01/01/2000', 'DD/MM/YYYY') WHERE not exists (SELECT 1 FROM dwh_history_timekeeping WHERE var_name ='lastOsinfoUpdate');

INSERT INTO dwh_history_timekeeping SELECT 'heartBeat',NULL,to_timestamp('01/01/2000', 'DD/MM/YYYY') WHERE not exists (SELECT 1 FROM dwh_history_timekeeping WHERE var_name ='heartBeat');

INSERT INTO dwh_history_timekeeping(var_name,var_value) SELECT 'DwhCurrentlyRunning','0' WHERE not exists (SELECT 1 FROM dwh_history_timekeeping WHERE var_name = 'DwhCurrentlyRunning');

INSERT INTO dwh_history_timekeeping(var_name,var_value) SELECT 'dwhHostname',NULL WHERE not exists (SELECT 1 FROM dwh_history_timekeeping WHERE var_name = 'dwhHostname');

INSERT INTO dwh_history_timekeeping(var_name,var_value) SELECT 'dwhUuid',NULL WHERE not exists (SELECT 1 FROM dwh_history_timekeeping WHERE var_name = 'dwhUuid');

INSERT INTO dwh_history_timekeeping SELECT 'lastSync',NULL,to_timestamp('01/01/2000', 'DD/MM/YYYY') WHERE not exists (SELECT 1 FROM dwh_history_timekeeping WHERE var_name ='lastSync');


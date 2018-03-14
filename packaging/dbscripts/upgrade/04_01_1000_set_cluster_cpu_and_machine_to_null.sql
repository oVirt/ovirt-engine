ALTER TABLE cluster ALTER COLUMN cpu_name SET DEFAULT NULL;
ALTER TABLE cluster ALTER COLUMN emulated_machine SET DEFAULT NULL;

UPDATE cluster SET cpu_name = NULL WHERE cpu_name ='';
UPDATE cluster SET emulated_machine = NULL WHERE emulated_machine = '';


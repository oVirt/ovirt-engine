SELECT fn_db_add_column('vds_groups', 'detect_emulated_machine', 'BOOLEAN DEFAULT FALSE');
UPDATE vds_groups set detect_emulated_machine = 'true' where emulated_machine is null;


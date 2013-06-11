SELECT fn_db_add_column('vds_dynamic', 'supported_emulated_machines', 'character varying(255)');
SELECT fn_db_add_column('vds_groups', 'emulated_machine', 'character varying(40)');

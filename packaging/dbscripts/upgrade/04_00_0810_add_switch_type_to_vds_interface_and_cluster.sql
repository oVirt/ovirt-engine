SELECT fn_db_add_column('vds_interface', 'reported_switch_type', 'VARCHAR(6)');
SELECT fn_db_add_column('cluster', 'switch_type', 'VARCHAR(6) NOT NULL DEFAULT ''legacy''');

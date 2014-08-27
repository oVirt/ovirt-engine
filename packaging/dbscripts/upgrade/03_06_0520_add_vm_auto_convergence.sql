SELECT fn_db_add_column('vm_static', 'is_auto_converge', 'BOOLEAN DEFAULT NULL');
SELECT fn_db_add_column('vm_static', 'is_migrate_compressed', 'BOOLEAN DEFAULT NULL');

SELECT fn_db_add_column('vds_groups', 'is_auto_converge', 'BOOLEAN DEFAULT NULL');
SELECT fn_db_add_column('vds_groups', 'is_migrate_compressed', 'BOOLEAN DEFAULT NULL');

SELECT fn_db_drop_column('vm_static', 'numatune_mode');
SELECT fn_db_add_column('numa_node', 'numa_tune_mode', 'varchar(20)');

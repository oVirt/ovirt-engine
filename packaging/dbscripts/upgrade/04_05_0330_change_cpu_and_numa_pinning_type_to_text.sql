SELECT fn_db_change_column_type('vm_dynamic', 'current_cpu_pinning', 'VARCHAR', 'TEXT');
SELECT fn_db_change_column_type('vm_dynamic', 'current_numa_pinning', 'VARCHAR', 'TEXT');

SELECT fn_db_change_column_type('vm_static', 'cpu_pinning', 'VARCHAR', 'TEXT');

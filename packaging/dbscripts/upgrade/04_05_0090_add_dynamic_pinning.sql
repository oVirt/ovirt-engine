SELECT fn_db_add_column('vm_dynamic', 'current_cpu_pinning', 'VARCHAR(4000)');
SELECT fn_db_add_column('vm_dynamic', 'current_sockets', 'INTEGER NOT NULL DEFAULT 0');
SELECT fn_db_add_column('vm_dynamic', 'current_cores', 'INTEGER NOT NULL DEFAULT 0');
SELECT fn_db_add_column('vm_dynamic', 'current_threads', 'INTEGER NOT NULL DEFAULT 0');

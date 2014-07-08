select fn_db_add_column('vm_statistics', 'memory_usage_history', 'TEXT DEFAULT NULL');
select fn_db_add_column('vm_statistics', 'cpu_usage_history', 'TEXT DEFAULT NULL');
select fn_db_add_column('vm_statistics', 'network_usage_history', 'TEXT DEFAULT NULL');

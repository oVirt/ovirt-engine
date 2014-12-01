select fn_db_add_column('vm_dynamic', 'guest_mem_free', 'INTEGER DEFAULT NULL');
select fn_db_add_column('vm_dynamic', 'guest_mem_buffered', 'INTEGER DEFAULT NULL');
select fn_db_add_column('vm_dynamic', 'guest_mem_cached', 'INTEGER DEFAULT NULL');

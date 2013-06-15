select fn_db_add_column('vm_static', 'quota_id', 'UUID');
select fn_db_add_column('images', 'quota_id', 'UUID');
select fn_db_add_column('storage_pool', 'quota_enforcement_type', 'INTEGER');
select fn_db_add_column('audit_log', 'quota_id', 'UUID');
select fn_db_add_column('audit_log', 'quota_name', 'VARCHAR(60)');
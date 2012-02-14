SELECT fn_db_add_column('audit_log', 'correlation_id', 'VARCHAR(50)');
SELECT fn_db_add_column('audit_log', 'job_id', 'UUID');

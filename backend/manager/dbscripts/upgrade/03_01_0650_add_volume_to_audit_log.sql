select fn_db_add_column('audit_log', 'gluster_volume_id', 'uuid');
select fn_db_add_column('audit_log', 'gluster_volume_name', 'VARCHAR(1000)');

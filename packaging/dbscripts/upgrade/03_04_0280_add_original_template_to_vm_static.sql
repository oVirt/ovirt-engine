select fn_db_add_column('vm_static', 'original_template_id', 'UUID default null');
select fn_db_add_column('vm_static', 'original_template_name', 'VARCHAR(255) default null');

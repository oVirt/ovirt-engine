 select fn_db_add_column('audit_log','brick_id','uuid');
 select fn_db_add_column('audit_log','brick_path','text DEFAULT ''''::text');

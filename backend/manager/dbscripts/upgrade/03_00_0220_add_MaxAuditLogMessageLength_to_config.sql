--change audit_log table message column from varchar to text.
select fn_db_change_column_type('audit_log','message','varchar','text');

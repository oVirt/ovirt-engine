--change audit_log table message column from varchar to text.
select fn_db_change_column_type('audit_log','message','varchar','text');
-- Max length of an audit log message  
select fn_db_add_config_value('MaxAuditLogMessageLength','10000','general');


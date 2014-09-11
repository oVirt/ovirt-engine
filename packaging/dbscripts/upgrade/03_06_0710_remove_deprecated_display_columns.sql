-- Drops unused graphics-related column superseded by graphics info

select fn_db_drop_column('vm_dynamic', 'display');
select fn_db_drop_column('vm_dynamic', 'display_secure_port');
select fn_db_drop_column('vm_dynamic', 'display_ip');
select fn_db_drop_column('vm_dynamic', 'display_type');


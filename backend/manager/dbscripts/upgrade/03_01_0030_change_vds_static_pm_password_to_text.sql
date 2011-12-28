-- change vm_ip in table vm_dynamic from char(255) to blob

select fn_db_change_column_type('vds_static','pm_password','VARCHAR','text');

-- change vm_ip in table vm_dynamic from char(255) to blob

select fn_db_change_column_type('vm_dynamic','vm_ip','VARCHAR','text');
